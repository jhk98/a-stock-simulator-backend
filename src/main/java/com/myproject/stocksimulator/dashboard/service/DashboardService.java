package com.myproject.stocksimulator.dashboard.service;

import com.myproject.stocksimulator.domain.Account;
import com.myproject.stocksimulator.domain.CurrentPrice;
import com.myproject.stocksimulator.domain.Holding;
import com.myproject.stocksimulator.domain.Member;
import com.myproject.stocksimulator.stock.domain.Stock;
import com.myproject.stocksimulator.stock.domain.StockType;
import com.myproject.stocksimulator.dashboard.dto.DashboardResponse;
import com.myproject.stocksimulator.dashboard.dto.DashboardResponse.AccountView;
import com.myproject.stocksimulator.dashboard.dto.DashboardResponse.HoldingView;
import com.myproject.stocksimulator.dashboard.dto.DashboardResponse.StockView;
import com.myproject.stocksimulator.repository.AccountRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final long INITIAL_BALANCE = 100_000_000L;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final AccountRepository accountRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 내 모의계좌 개설
     *
     * <p>
     * 회원 행을 잠근 뒤 기존 계좌를 확인한다.
     * 중복 요청에도 기존 계좌를 반환하며 초기자금을 재지급하지 않는다.
     * </p>
     */
    @Transactional
    public AccountView openAccount(Long memberId) {
        requireMemberId(memberId);

        Member member = entityManager.find(
                Member.class,
                memberId,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (member == null) {
            throw new BadCredentialsException(
                    "존재하지 않는 회원입니다."
            );
        }

        Account existing = accountRepository.findByMemberId(memberId)
                .orElse(null);

        if (existing != null) {
            return toAccountView(existing);
        }

        // 확정된 DATETIME 컬럼에 맞춰 초 단위로 저장한다.
        LocalDateTime now = LocalDateTime.now(SEOUL).withNano(0);

        // 회원 ID는 고유하므로 생성 계좌번호도 고유하다.
        // 최대 24자로 account_num VARCHAR(30) 범위에 들어간다.
        String accountNum = String.format(
                Locale.ROOT,
                "SIM-%020d",
                memberId
        );

        Account account = Account.builder()
                .member(member)
                .accountNum(accountNum)
                .deposit(INITIAL_BALANCE)
                .createdAt(now)
                .modifiedAt(now)
                .build();

        accountRepository.saveAndFlush(account);

        return toAccountView(account);
    }

    /**
     * 로그인한 회원의 메인 화면 데이터
     *
     * <p> 외부 API를 직접 호출하지 않고 저장된 종목 및 시세를 조회한다. </p>
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long memberId) {
        requireMemberId(memberId);

        Account account = accountRepository.findByMemberId(memberId)
                .orElse(null);

        List<Holding> holdingRows = account == null
                ? List.of()
                : entityManager.createQuery("""
                        select h
                        from Holding h
                        join fetch h.stock
                        where h.account.id = :accountId
                        order by h.stock.stockCode, h.id
                        """, Holding.class)
                .setParameter("accountId", account.getId())
                .getResultList();

        /*
         * 현재 테이블에는 (account_id, stock_id) UNIQUE가 없다.
         * 따라서 같은 종목이 여러 행에 있으면 수량과 매입금액을 합산한다.
         *
         * 조회 결과만 합산하며 DB의 보유 행을 수정하지 않는다.
         */
        Map<Long, Position> positions = new LinkedHashMap<>();

        for (Holding holding : holdingRows) {
            Stock stock = holding.getStock();

            BigDecimal purchaseCost = holding.getAvgPrice()
                    .multiply(BigDecimal.valueOf(holding.getQuantity()));

            Position incoming = new Position(
                    stock,
                    holding.getQuantity(),
                    purchaseCost
            );

            positions.merge(
                    stock.getId(),
                    incoming,
                    (previous, added) -> new Position(
                            previous.stock(),
                            Math.addExact(
                                    previous.quantity(),
                                    added.quantity()
                            ),
                            previous.purchaseCost().add(
                                    added.purchaseCost()
                            )
                    )
            );
        }

        List<Stock> featured = findFeaturedStocks();

        Set<Long> stockIds = new LinkedHashSet<>(
                positions.keySet()
        );

        for (Stock stock : featured) {
            stockIds.add(stock.getId());
        }

        Map<Long, CurrentPrice> prices = findPrices(stockIds);

        List<HoldingView> holdingViews = new ArrayList<>();
        BigDecimal holdingValue = BigDecimal.ZERO;
        boolean valuationComplete = true;

        for (Position position : positions.values()) {
            Stock stock = position.stock();
            CurrentPrice price = usablePrice(
                    prices.get(stock.getId())
            );

            BigDecimal quantity = BigDecimal.valueOf(
                    position.quantity()
            );

            // 표시용 평균단가. 확정된 avg_price와 동일하게 소수점 6자리.
            BigDecimal averagePrice = position.purchaseCost()
                    .divide(quantity, 6, RoundingMode.HALF_UP);

            BigDecimal marketValue = null;
            BigDecimal unrealizedProfit = null;

            if (price == null) {
                valuationComplete = false;
            } else {
                marketValue = BigDecimal.valueOf(
                        price.getCurrentPrice()
                ).multiply(quantity);

                /*
                 * 표시용 평균단가를 다시 곱하지 않고 원래 합산 매입금액을
                 * 사용하여 추가적인 반올림 오차를 피한다.
                 */
                unrealizedProfit = marketValue.subtract(
                        position.purchaseCost()
                );

                holdingValue = holdingValue.add(marketValue);
            }

            holdingViews.add(new HoldingView(
                    stock.getStockCode(),
                    stock.getStockName(),
                    stock.getStockType().name(),
                    position.quantity(),
                    averagePrice,
                    price == null ? null : price.getCurrentPrice(),
                    marketValue,
                    unrealizedProfit,
                    price == null ? null : price.getUpdatedAt()
            ));
        }

        List<StockView> featuredViews = featured.stream()
                .map(stock -> {
                    CurrentPrice price = usablePrice(
                            prices.get(stock.getId())
                    );

                    return new StockView(
                            stock.getStockCode(),
                            stock.getStockName(),
                            stock.getStockType().name(),
                            price == null ? null : price.getCurrentPrice(),
                            price == null ? null : price.getChangeRate(),
                            price == null ? null : price.getCurrentVolume(),
                            price == null ? null : price.getUpdatedAt()
                    );
                })
                .toList();

        BigDecimal totalAssets = null;
        BigDecimal totalProfit = null;

        if (account != null && valuationComplete) {
            totalAssets = BigDecimal.valueOf(account.getDeposit())
                    .add(holdingValue);

            totalProfit = totalAssets.subtract(
                    BigDecimal.valueOf(INITIAL_BALANCE)
            );
        }

        return new DashboardResponse(
                INITIAL_BALANCE,
                account == null ? null : toAccountView(account),
                List.copyOf(holdingViews),
                featuredViews,
                valuationComplete,
                valuationComplete ? holdingValue : null,
                totalAssets,
                totalProfit
        );
    }

    /**
     * 저장된 거래량 기준으로 시장별 최대 3개를 조회한다.
     * 현재 시점의 실시간 거래량 순위라는 의미는 아니다.
     */
    private List<Stock> findFeaturedStocks() {
        List<Stock> result = new ArrayList<>();

        for (StockType market : StockType.values()) {
            List<Stock> stocks = entityManager.createQuery("""
                    select s
                    from Stock s
                    left join CurrentPrice p on p.stock = s
                    where s.stockType = :market
                    order by coalesce(p.currentVolume, 0) desc,
                             s.stockCode asc
                    """, Stock.class)
                    .setParameter("market", market)
                    .setMaxResults(3)
                    .getResultList();

            result.addAll(stocks);
        }

        return result;
    }

    /**
     * 화면에 필요한 시세를 한 번에 조회한다.
     */
    private Map<Long, CurrentPrice> findPrices(Set<Long> stockIds) {
        Map<Long, CurrentPrice> result = new HashMap<>();

        if (stockIds.isEmpty()) {
            return result;
        }

        List<CurrentPrice> prices = entityManager.createQuery("""
                select p
                from CurrentPrice p
                where p.stock.id in :stockIds
                """, CurrentPrice.class)
                .setParameter("stockIds", stockIds)
                .getResultList();

        for (CurrentPrice price : prices) {
            result.put(price.getStock().getId(), price);
        }

        return result;
    }

    /**
     * 확정된 테이블의 가격·거래량 조건과 일치하는 시세만 사용한다.
     */
    private CurrentPrice usablePrice(CurrentPrice price) {
        if (price == null
                || price.getCurrentPrice() == null
                || price.getCurrentPrice() <= 0
                || price.getCurrentVolume() == null
                || price.getCurrentVolume() <= 0
                || price.getUpdatedAt() == null) {
            return null;
        }

        return price;
    }

    private AccountView toAccountView(Account account) {
        return new AccountView(
                account.getId(),
                account.getAccountNum(),
                account.getDeposit(),
                account.getCreatedAt()
        );
    }

    private void requireMemberId(Long memberId) {
        if (memberId == null) {
            throw new BadCredentialsException(
                    "로그인이 필요합니다."
            );
        }
    }

    /**
     * 동일 종목의 보유 내역 합산용 내부 데이터.
     */
    private record Position(
            Stock stock,
            long quantity,
            BigDecimal purchaseCost
    ) {
    }
}