package com.myproject.stocksimulator.stock.service;

import com.myproject.stocksimulator.entity.CurrentPrice;
import com.myproject.stocksimulator.kiwoom.KiwoomApiException;
import com.myproject.stocksimulator.kiwoom.KiwoomAuthService;
import com.myproject.stocksimulator.kiwoom.client.KiwoomHttpClient;
import com.myproject.stocksimulator.market.service.MarketService;
import com.myproject.stocksimulator.stock.entity.Stock;
import com.myproject.stocksimulator.stock.entity.StockType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.IntStream;

/**
 * 현재가 조회와 마지막 수집 시세 저장
 *
 * 외부 API 호출과 DB 저장 트랜잭션을 분리한다.
 * 주문 체결가격은 클라이언트 입력이 아닌 이 서비스의 시세를 사용한다.
 */
@Service
public class QuoteService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final KiwoomHttpClient http;
    private final KiwoomAuthService auth;
    private final MarketService market;
    private final TransactionTemplate transaction;

    @PersistenceContext
    private EntityManager entityManager;

    // 같은 서버에서 동일 종목을 동시에 수집하는 일을 줄인다.
    private final Object[] locks = IntStream.range(0, 64)
            .mapToObj(index -> new Object())
            .toArray();

    public QuoteService(
            KiwoomHttpClient http,
            KiwoomAuthService auth,
            MarketService market,
            PlatformTransactionManager transactionManager
    ) {
        this.http = http;
        this.auth = auth;
        this.market = market;

        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        this.transaction.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
    }

    public record StockInfo(
            long id,
            String stockCode,
            String stockName,
            String market
    ) {
    }

    public record Quote(
            long stockId,
            String stockCode,
            String stockName,
            String market,
            long currentPrice,
            long changePrice,
            BigDecimal changeRate,
            long volume,
            Instant fetchedAt
    ) {
    }

    public StockInfo stock(String code) {
        validateCode(code);

        return transaction.execute(status ->
                entityManager.createQuery("""
                        select s.id, s.stockCode, s.stockName, s.stockType
                        from Stock s
                        where s.stockCode = :code
                        """, Object[].class)
                        .setParameter("code", code)
                        .getResultList()
                        .stream()
                        .map(row -> new StockInfo(
                                ((Number) row[0]).longValue(),
                                (String) row[1],
                                (String) row[2],
                                ((StockType) row[3]).name()
                        ))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "등록된 국내 종목이 아닙니다."
                        ))
        );
    }

    /**
     * 외부 API를 호출하지 않고 저장된 시세만 읽는다.
     * 엔티티 대신 필요한 컬럼을 조회해 DTO로 반환한다.
     */
    public Quote saved(String code) {
        validateCode(code);

        return transaction.execute(status ->
                entityManager.createQuery("""
                        select s.id, s.stockCode, s.stockName, s.stockType,
                               p.currentPrice, p.changePrice, p.changeRate,
                               p.currentVolume, p.updatedAt
                        from CurrentPrice p
                        join p.stock s
                        where s.stockCode = :code
                        """, Object[].class)
                        .setParameter("code", code)
                        .getResultList()
                        .stream()
                        .map(this::toQuote)
                        .findFirst()
                        .orElse(null)
        );
    }

    /**
     * 조회 시각뿐 아니라 가격·거래량도 확인한다.
     * 거래량 0은 정상적으로 허용한다.
     */
    public static boolean fresh(Quote quote, long seconds) {
        if (quote == null || quote.fetchedAt() == null
                || quote.currentPrice() <= 0 || quote.volume() < 0) {
            return false;
        }

        Instant now = Instant.now();

        return !quote.fetchedAt().isAfter(now.plusSeconds(5))
                && quote.fetchedAt().isAfter(now.minusSeconds(seconds));
    }

    public Quote quote(String code, boolean force) {
        StockInfo stock = stock(code);
        Quote cached = saved(code);

        if (!force && fresh(cached, 10)) {
            return cached;
        }

        synchronized (locks[Math.floorMod(code.hashCode(), locks.length)]) {
            cached = saved(code);

            if (!force && fresh(cached, 10)) {
                return cached;
            }

            // 외부 통신은 DB 트랜잭션 밖에서 수행한다.
            Map<?, ?> body = request(code);

            try {
                if (!"0".equals(String.valueOf(body.get("return_code")))
                        || !code.equals(
                        String.valueOf(body.get("stk_cd")).trim()
                )) {
                    throw KiwoomApiException.invalidResponse();
                }

                long price = decimal(body, "cur_prc").abs().longValueExact();
                long change = decimal(body, "pred_pre").longValueExact();

                BigDecimal rate = decimal(body, "flu_rt")
                        .setScale(4, RoundingMode.HALF_UP);

                long volume = decimal(body, "trde_qty").longValueExact();

                if (price <= 0 || volume < 0 || rate.precision() > 10) {
                    throw KiwoomApiException.invalidResponse();
                }

                LocalDateTime received = LocalDateTime.now(SEOUL)
                        .withNano(0);

                return transaction.execute(status -> {
                    /*
                     * 시세 행이 아직 없을 수도 있으므로 종목 행을 잠근다.
                     * 여러 서버에서 수집해도 최초 시세 행 생성을 직렬화한다.
                     */
                    Stock managedStock = entityManager.find(
                            Stock.class,
                            stock.id(),
                            LockModeType.PESSIMISTIC_WRITE
                    );

                    if (managedStock == null) {
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "종목을 찾을 수 없습니다."
                        );
                    }

                    CurrentPrice current = entityManager.createQuery("""
                            select p
                            from CurrentPrice p
                            where p.stock.id = :stockId
                            """, CurrentPrice.class)
                            .setParameter("stockId", stock.id())
                            .getResultList()
                            .stream()
                            .findFirst()
                            .orElse(null);

                    if (current == null) {
                        current = CurrentPrice.builder()
                                .stock(managedStock)
                                .currentPrice(price)
                                .changePrice(change)
                                .changeRate(rate)
                                .currentVolume(volume)
                                .updatedAt(received)
                                .build();

                        entityManager.persist(current);
                    } else {
                        // 영속성 컨텍스트에 예전 값이 있으면 DB 값으로 갱신한다.
                        entityManager.refresh(current);

                        // 먼저 수집한 응답이 더 늦게 저장되는 경우를 막는다.
                        if (!current.getUpdatedAt().isAfter(received)) {
                            current.refresh(
                                    price,
                                    change,
                                    rate,
                                    volume,
                                    received
                            );
                        }
                    }

                    entityManager.flush();

                    return new Quote(
                            managedStock.getId(),
                            managedStock.getStockCode(),
                            managedStock.getStockName(),
                            managedStock.getStockType().name(),
                            current.getCurrentPrice(),
                            current.getChangePrice(),
                            current.getChangeRate(),
                            current.getCurrentVolume(),
                            current.getUpdatedAt().atZone(SEOUL).toInstant()
                    );
                });
            } catch (NumberFormatException | ArithmeticException exception) {
                throw KiwoomApiException.invalidResponse();
            }
        }
    }

    private Quote toQuote(Object[] row) {
        return new Quote(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                ((StockType) row[3]).name(),
                ((Number) row[4]).longValue(),
                ((Number) row[5]).longValue(),
                (BigDecimal) row[6],
                ((Number) row[7]).longValue(),
                ((LocalDateTime) row[8]).atZone(SEOUL).toInstant()
        );
    }

    private Map<?, ?> request(String code) {
        for (int attempt = 0; attempt < 2; attempt++) {
            String token = auth.getAccessToken();

            try {
                Map<?, ?> body = http.post(
                        "/api/dostk/stkinfo",
                        "ka10001",
                        token,
                        null,
                        Map.of("stk_cd", code),
                        Map.class
                ).getBody();

                if (body == null) {
                    throw KiwoomApiException.invalidResponse();
                }

                return body;
            } catch (KiwoomApiException exception) {
                if (exception.upstreamStatus() == 401) {
                    auth.invalidateIfSame(token);

                    if (attempt == 0) {
                        continue;
                    }
                }

                throw exception;
            }
        }

        throw KiwoomApiException.invalidResponse();
    }

    private BigDecimal decimal(Map<?, ?> body, String key) {
        Object value = body.get(key);

        if (value == null || value.toString().isBlank()) {
            throw KiwoomApiException.invalidResponse();
        }

        return new BigDecimal(value.toString().trim());
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank() || code.length() > 20) {
            throw new IllegalArgumentException(
                    "종목코드가 올바르지 않습니다."
            );
        }
    }

    public List<StockInfo> featuredStocks() {
        return market.ranking().items().stream()
                .limit(6)
                .map(item -> new StockInfo(
                        item.stockId(),
                        item.stockCode(),
                        item.stockName(),
                        item.market()
                ))
                .toList();
    }

    /**
     * 기존 자동 수집 범위를 유지한다.
     * 보유 종목과 거래대금 상위 종목을 주기적으로 갱신한다.
     */
    public List<String> trackedCodes() {
        List<String> heldCodes = transaction.execute(status ->
                entityManager.createQuery("""
                        select distinct s.stockCode
                        from Holding h
                        join h.stock s
                        order by s.stockCode
                        """, String.class)
                        .getResultList()
        );

        LinkedHashSet<String> codes = new LinkedHashSet<>(
                Objects.requireNonNull(heldCodes)
        );

        for (StockInfo stock : featuredStocks()) {
            codes.add(stock.stockCode());
        }

        return List.copyOf(codes);
    }
}