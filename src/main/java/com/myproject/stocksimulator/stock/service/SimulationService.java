package com.myproject.stocksimulator.stock.service;

import com.myproject.stocksimulator.account.entity.Account;
import com.myproject.stocksimulator.entity.Holding;
import com.myproject.stocksimulator.stock.entity.orders.Orders;
import com.myproject.stocksimulator.stock.entity.orders.OrderType;
import com.myproject.stocksimulator.stock.entity.stock.Stock;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 투자 현황, 모의 매매, 주문내역
 *
 * DB 처리는 JPA 엔티티를 사용한다.
 * 잔액·보유수량·주문기록은 하나의 트랜잭션으로 반영한다.
 */
@Service
public class SimulationService {

    private final QuoteService quotes;
    private final TransactionTemplate transaction;

    @PersistenceContext
    private EntityManager entityManager;

    public SimulationService(
            QuoteService quotes,
            PlatformTransactionManager transactionManager
    ) {
        this.quotes = quotes;

        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
    }

    /**
     * 주문 요청 DTO
     *
     * requestId는 재전송을 구분하는 식별자다.
     * 같은 주문을 다시 보낼 때는 같은 requestId를 사용한다.
     * 가격은 요청으로 받지 않고 서버에서 조회한다.
     */
    public record TradeRequest(
            @NotBlank
            @Pattern(regexp =
                    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
                            + "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
            String requestId,

            @NotBlank
            @Size(max = 20)
            String stockCode,

            @NotBlank
            @Pattern(regexp = "BUY|SELL")
            String side,

            @Min(1)
            @Max(1_000_000)
            int quantity
    ) {
    }

    public record AccountView(
            long id,
            String accountNum,
            long deposit
    ) {
    }

    public record HoldingView(
            String stockCode,
            String stockName,
            long quantity,
            BigDecimal avgPrice,
            QuoteService.Quote quote,
            boolean fresh,
            BigDecimal marketValue,
            BigDecimal unrealizedProfit
    ) {
    }

    public record FeaturedView(
            String stockCode,
            String stockName,
            String market,
            QuoteService.Quote quote,
            boolean fresh
    ) {
    }

    public record Overview(
            long initialBalance,
            AccountView account,
            List<HoldingView> holdings,
            List<FeaturedView> featuredStocks,
            boolean valuationComplete,
            BigDecimal totalAssets,
            BigDecimal totalProfit
    ) {
    }

    public record OrderView(
            long id,
            String stockCode,
            String stockName,
            String side,
            long price,
            int quantity,
            String status,
            LocalDateTime createdAt
    ) {
    }

    public record OrderPage(
            List<OrderView> items,
            Long nextBefore
    ) {
    }

    // 동일 종목의 보유 행이 여러 개일 때 합산하기 위한 내부 DTO.
    private record Position(
            String stockCode,
            String stockName,
            long quantity,
            BigDecimal cost
    ) {
    }

    // 외부 시세를 조회하는 동안 계좌가 리셋되었는지 확인할 기준.
    private record TradeCheck(
            long resetVersion,
            OrderView existing
    ) {
    }

    /**
     * 계좌를 잠가 같은 계좌에 대한 동시 매매를 순서대로 처리한다.
     * 반드시 transaction.execute 안에서 호출한다.
     */
    private Account lockedAccount(long memberId) {
        Account account = entityManager.createQuery("""
                select a
                from Account a
                where a.member.id = :memberId
                """, Account.class)
                .setParameter("memberId", memberId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);

        if (account != null) {
            // 이전 조회로 남아 있는 엔티티 값 대신 잠금 이후 DB 값을 사용한다.
            entityManager.refresh(
                    account,
                    LockModeType.PESSIMISTIC_WRITE
            );
        }

        return account;
    }

    public Overview overview(long memberId) {
        return transaction.execute(status -> {
            Account account = lockedAccount(memberId);

            List<HoldingView> holdings = new ArrayList<>();
            BigDecimal holdingValue = BigDecimal.ZERO;
            boolean complete = account != null;

            if (account != null) {
                Map<Long, Position> positions = new LinkedHashMap<>();

                List<Holding> rows = entityManager.createQuery("""
                        select h
                        from Holding h
                        join fetch h.stock
                        where h.account.id = :accountId
                        order by h.stock.stockCode, h.id
                        """, Holding.class)
                        .setParameter("accountId", account.getId())
                        .getResultList();

                /*
                 * 확정 DB에는 계좌·종목 UNIQUE가 없다.
                 * 따라서 같은 종목의 여러 행을 합산해서 보여준다.
                 */
                for (Holding holding : rows) {
                    Stock stock = holding.getStock();

                    BigDecimal cost = holding.getAvgPrice().multiply(
                            BigDecimal.valueOf(holding.getQuantity())
                    );

                    Position incoming = new Position(
                            stock.getStockCode(),
                            stock.getStockName(),
                            holding.getQuantity(),
                            cost
                    );

                    positions.merge(
                            stock.getId(),
                            incoming,
                            (left, right) -> new Position(
                                    left.stockCode(),
                                    left.stockName(),
                                    Math.addExact(
                                            left.quantity(),
                                            right.quantity()
                                    ),
                                    left.cost().add(right.cost())
                            )
                    );
                }

                for (Position position : positions.values()) {
                    QuoteService.Quote quote = quotes.saved(
                            position.stockCode()
                    );

                    boolean fresh = QuoteService.fresh(quote, 120);
                    BigDecimal quantity = BigDecimal.valueOf(
                            position.quantity()
                    );

                    BigDecimal average = position.cost().divide(
                            quantity,
                            6,
                            RoundingMode.HALF_UP
                    );

                    BigDecimal value = fresh
                            ? BigDecimal.valueOf(quote.currentPrice())
                            .multiply(quantity)
                            : null;

                    BigDecimal profit = value == null
                            ? null
                            : value.subtract(position.cost());

                    if (value == null) {
                        complete = false;
                    } else {
                        holdingValue = holdingValue.add(value);
                    }

                    holdings.add(new HoldingView(
                            position.stockCode(),
                            position.stockName(),
                            position.quantity(),
                            average,
                            quote,
                            fresh,
                            value,
                            profit
                    ));
                }
            }

            List<FeaturedView> featured = new ArrayList<>();

            for (QuoteService.StockInfo stock : quotes.featuredStocks()) {
                QuoteService.Quote quote = quotes.saved(stock.stockCode());

                featured.add(new FeaturedView(
                        stock.stockCode(),
                        stock.stockName(),
                        stock.market(),
                        quote,
                        QuoteService.fresh(quote, 120)
                ));
            }

            BigDecimal totalAssets = complete
                    ? holdingValue.add(
                    BigDecimal.valueOf(account.getDeposit())
            )
                    : null;

            return new Overview(
                    Account.INITIAL_BALANCE,
                    account == null ? null : new AccountView(
                            account.getId(),
                            account.getAccountNum(),
                            account.getDeposit()
                    ),
                    List.copyOf(holdings),
                    List.copyOf(featured),
                    complete,
                    totalAssets,
                    totalAssets == null
                            ? null
                            : totalAssets.subtract(
                            BigDecimal.valueOf(Account.INITIAL_BALANCE)
                    )
            );
        });
    }

    public OrderView trade(long memberId, TradeRequest request) {
        // HTTP 검증과 별개로 서비스에서도 거래 방향·수량을 확인한다.
        validateTrade(request);

        /*
         * 먼저 기존 주문을 확인한다.
         * 성공한 주문 재전송이라면 외부 시세를 다시 조회하지 않는다.
         */
        TradeCheck check = Objects.requireNonNull(
                transaction.execute(status -> {
                    Account account = lockedAccount(memberId);

                    if (account == null) {
                        throw badRequest("먼저 모의계좌를 개설해주세요.");
                    }

                    return new TradeCheck(
                            account.getResetVersion(),
                            existingOrder(account.getId(), request)
                    );
                })
        );

        if (check.existing() != null) {
            return check.existing();
        }

        // 외부 시세 조회 중에는 계좌 잠금을 유지하지 않는다.
        QuoteService.Quote quote = quotes.quote(
                request.stockCode(),
                true
        );

        return transaction.execute(status -> {
            Account account = lockedAccount(memberId);

            if (account == null) {
                throw badRequest("모의계좌가 없습니다.");
            }

            // 계좌 잠금을 기다리는 동안 같은 주문이 완료됐을 수 있다.
            OrderView duplicate = existingOrder(account.getId(), request);

            if (duplicate != null) {
                return duplicate;
            }

            if (account.getResetVersion() != check.resetVersion()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "시세 조회 중 계좌가 변경되었습니다. 계좌를 다시 확인해주세요."
                );
            }

            if (!QuoteService.fresh(quote, 15)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "시세 조회 후 시간이 지났습니다. 같은 주문으로 다시 시도해주세요."
                );
            }

            long total = Math.multiplyExact(
                    quote.currentPrice(),
                    (long) request.quantity()
            );

            List<Holding> rows = entityManager.createQuery("""
                    select h
                    from Holding h
                    where h.account.id = :accountId
                      and h.stock.id = :stockId
                    order by h.id
                    """, Holding.class)
                    .setParameter("accountId", account.getId())
                    .setParameter("stockId", quote.stockId())
                    .getResultList();

            long oldQuantity = 0;
            BigDecimal oldCost = BigDecimal.ZERO;

            for (Holding holding : rows) {
                oldQuantity = Math.addExact(
                        oldQuantity,
                        holding.getQuantity()
                );

                oldCost = oldCost.add(
                        holding.getAvgPrice().multiply(
                                BigDecimal.valueOf(holding.getQuantity())
                        )
                );
            }

            int nextQuantity;
            BigDecimal nextAverage;

            if ("BUY".equals(request.side())) {
                if (account.getDeposit() < total) {
                    throw badRequest("예수금이 부족합니다.");
                }

                nextQuantity = Math.toIntExact(
                        Math.addExact(oldQuantity, request.quantity())
                );

                nextAverage = oldCost.add(BigDecimal.valueOf(total))
                        .divide(
                                BigDecimal.valueOf(nextQuantity),
                                6,
                                RoundingMode.HALF_UP
                        );
            } else {
                if (oldQuantity < request.quantity()) {
                    throw badRequest("보유수량이 부족합니다.");
                }

                nextQuantity = Math.toIntExact(
                        oldQuantity - request.quantity()
                );

                nextAverage = oldCost.divide(
                        BigDecimal.valueOf(oldQuantity),
                        6,
                        RoundingMode.HALF_UP
                );
            }

            if (nextQuantity > 0
                    && (nextAverage.signum() <= 0
                    || nextAverage.precision() > 19)) {
                throw badRequest("평균 매입가의 저장 범위를 초과했습니다.");
            }

            Stock stock = entityManager.getReference(
                    Stock.class,
                    quote.stockId()
            );

            /*
             * 계좌 엔티티의 업무 메서드로 잔액을 변경한다.
             * 아래 처리 중 예외가 발생하면 모든 변경이 함께 롤백된다.
             */
            if ("BUY".equals(request.side())) {
                account.withdraw(total);
            } else {
                account.credit(total);
            }

            /*
             * 기존 보유 행이 여러 개면 첫 행에 합산하고 나머지를 삭제한다.
             * 전량 매도일 때는 수량 0을 저장하지 않고 모두 삭제한다.
             */
            if (nextQuantity == 0) {
                rows.forEach(entityManager::remove);
            } else if (rows.isEmpty()) {
                entityManager.persist(
                        Holding.builder()
                                .account(account)
                                .stock(stock)
                                .quantity(nextQuantity)
                                .avgPrice(nextAverage)
                                .build()
                );
            } else {
                rows.getFirst().changePosition(
                        nextQuantity,
                        nextAverage
                );

                for (int index = 1; index < rows.size(); index++) {
                    entityManager.remove(rows.get(index));
                }
            }

            /*
             * 별도 trade_request 테이블은 사용하지 않는다.
             * 확정된 orders.request_id에 요청 식별자를 저장한다.
             */
            Orders order = Orders.builder()
                    .account(account)
                    .stock(stock)
                    .requestId(normalizedRequestId(request))
                    .orderType(OrderType.valueOf(request.side()))
                    .orderPrice(quote.currentPrice())
                    .quantity(request.quantity())
                    .build();

            entityManager.persist(order);
            entityManager.flush();

            return toOrderView(order);
        });
    }

    /**
     * 같은 계좌의 requestId로 이미 체결된 주문을 조회한다.
     * 동일 ID를 다른 주문 내용에 재사용하면 거절한다.
     */
    private OrderView existingOrder(
            long accountId,
            TradeRequest request
    ) {
        Orders order = entityManager.createQuery("""
                select o
                from Orders o
                join fetch o.stock
                where o.account.id = :accountId
                  and o.requestId = :requestId
                """, Orders.class)
                .setParameter("accountId", accountId)
                .setParameter("requestId", normalizedRequestId(request))
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);

        if (order == null) {
            return null;
        }

        if (!order.getStock().getStockCode().equals(request.stockCode())
                || !order.getOrderType().name().equals(request.side())
                || order.getQuantity() != request.quantity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "같은 요청 식별자로 다른 주문을 보낼 수 없습니다."
            );
        }

        return toOrderView(order);
    }

    /**
     * 본인 계좌의 주문만 최신순으로 조회한다.
     * 26개를 읽어 다음 페이지 존재 여부를 판단하고 25개를 반환한다.
     */
    public OrderPage orders(long memberId, Long beforeId) {
        if (beforeId != null && beforeId <= 0) {
            throw badRequest("주문 조회 기준이 올바르지 않습니다.");
        }

        return transaction.execute(status -> {
            List<Orders> rows = entityManager.createQuery("""
                    select o
                    from Orders o
                    join fetch o.stock
                    where o.account.member.id = :memberId
                      and o.id < :beforeId
                    order by o.id desc
                    """, Orders.class)
                    .setParameter("memberId", memberId)
                    .setParameter(
                            "beforeId",
                            beforeId == null ? Long.MAX_VALUE : beforeId
                    )
                    .setMaxResults(26)
                    .getResultList();

            boolean hasNext = rows.size() > 25;

            List<OrderView> items = rows.stream()
                    .limit(25)
                    .map(this::toOrderView)
                    .toList();

            return new OrderPage(
                    items,
                    hasNext ? items.getLast().id() : null
            );
        });
    }

    private OrderView toOrderView(Orders order) {
        return new OrderView(
                order.getId(),
                order.getStock().getStockCode(),
                order.getStock().getStockName(),
                order.getOrderType().name(),
                order.getOrderPrice(),
                order.getQuantity(),
                // DB에는 성공한 체결만 저장하므로 응답 상태는 고정이다.
                "COMPLETE",
                order.getCreatedAt()
        );
    }

    private String normalizedRequestId(TradeRequest request) {
        return UUID.fromString(request.requestId()).toString();
    }

    private void validateTrade(TradeRequest request) {
        if (request == null
                || request.requestId() == null
                || !request.requestId().matches(
                "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
                        + "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
        )
                || request.stockCode() == null
                || request.stockCode().isBlank()
                || request.stockCode().length() > 20
                || (!"BUY".equals(request.side())
                && !"SELL".equals(request.side()))
                || request.quantity() < 1
                || request.quantity() > 1_000_000) {
            throw badRequest("주문 요청 값이 올바르지 않습니다.");
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }
}