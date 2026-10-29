package com.myproject.stocksimulator.stock.service;

import com.myproject.stocksimulator.stock.entity.currentPrice.CurrentPrice;
import com.myproject.stocksimulator.kiwoom.KiwoomApiException;
import com.myproject.stocksimulator.kiwoom.KiwoomAuthService;
import com.myproject.stocksimulator.kiwoom.client.KiwoomHttpClient;
import com.myproject.stocksimulator.market.service.MarketService;
import com.myproject.stocksimulator.stock.entity.stock.Stock;
import com.myproject.stocksimulator.stock.entity.stock.StockType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.IntStream;

/**
 * 현재가 조회와 마지막 수집 시세 저장.
 *
 * 외부 API 호출과 DB 트랜잭션을 분리한다.
 * DB의 updatedAt은 한국 현지 시각으로 저장하고,
 * 응답에서는 Instant로 변환한다.
 */
@Service
public class QuoteService {

    private static final Logger log =
            LoggerFactory.getLogger(QuoteService.class);

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // 여러 서버 사이의 작은 시계 차이를 허용하는 범위.
    private static final long FUTURE_TOLERANCE_SECONDS = 5;

    private final KiwoomHttpClient http;
    private final KiwoomAuthService auth;
    private final MarketService market;
    private final TransactionTemplate transaction;

    @PersistenceContext
    private EntityManager entityManager;

    // 같은 서버에서 동일 종목의 외부 조회가 겹치는 일을 줄인다.
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

        // 각 실행 인스턴스의 실제 JVM 시간 기준을 시작 로그에 남긴다.
        log.info(
                "[QUOTE][CLOCK_CONFIG] jvmZone={} storageZone={} nowUtc={}",
                ZoneId.systemDefault(),
                SEOUL,
                Instant.now()
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
     * 외부 API를 호출하지 않고 DB에 저장된 시세를 읽는다.
     * 이 메서드는 저장 시각을 현재 시각으로 변경하지 않는다.
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
     * 가격·거래량·수집 시각을 검사한다.
     *
     * 현재 주문 서비스는 seconds=15로 호출한다.
     * 주문 검사가 실패하면 실제 사유를 로그에 기록한다.
     */
    public static boolean fresh(Quote quote, long seconds) {
        Instant now = Instant.now();
        String reason = null;

        if (quote == null) {
            reason = "QUOTE_MISSING";
        } else if (quote.fetchedAt() == null) {
            reason = "TIMESTAMP_MISSING";
        } else if (quote.currentPrice() <= 0) {
            reason = "INVALID_PRICE";
        } else if (quote.volume() < 0) {
            reason = "INVALID_VOLUME";
        } else if (quote.fetchedAt().isAfter(
                now.plusSeconds(FUTURE_TOLERANCE_SECONDS)
        )) {
            reason = "FUTURE_TIMESTAMP";
        } else if (!quote.fetchedAt().isAfter(
                now.minusSeconds(seconds)
        )) {
            reason = "STALE_TIMESTAMP";
        }

        if (reason != null && seconds == 15) {
            Long ageMillis = quote == null || quote.fetchedAt() == null
                    ? null
                    : Duration.between(quote.fetchedAt(), now).toMillis();

            log.warn(
                    "[QUOTE][TRADE_QUOTE_REJECTED] "
                            + "stock={} reason={} fetchedAt={} "
                            + "checkedAt={} ageMillis={} maxAgeSeconds={}",
                    quote == null ? "-" : safeCode(quote.stockCode()),
                    reason,
                    quote == null ? null : quote.fetchedAt(),
                    now,
                    ageMillis,
                    seconds
            );
        }

        return reason == null;
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

                /*
                 * 정상 응답을 수신·검증한 시각이다.
                 * DB 잠금 대기 후의 현재 시각으로 덮어쓰지 않는다.
                 */
                LocalDateTime received = LocalDateTime.now(SEOUL)
                        .withNano(0);

                Quote result = transaction.execute(status -> {
                    // 최초 시세 생성과 동일 종목 갱신을 직렬화한다.
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
                        // 영속성 컨텍스트에 남은 예전 값 대신 DB 값을 읽는다.
                        entityManager.refresh(current);

                        LocalDateTime previousTime = current.getUpdatedAt();
                        LocalDateTime checkedAt = LocalDateTime.now(SEOUL);

                        boolean invalidFutureTime = previousTime != null
                                && previousTime.isAfter(
                                checkedAt.plusSeconds(
                                        FUTURE_TOLERANCE_SECONDS
                                )
                        );

                        /*
                         * 기존 값이 비정상적인 미래이면,
                         * 새로 검증한 가격·거래량·시각을 함께 저장해 회복한다.
                         *
                         * 정상적인 더 최신 시세는 기존처럼 유지한다.
                         */
                        boolean shouldUpdate = previousTime == null
                                || invalidFutureTime
                                || !previousTime.isAfter(received);

                        if (invalidFutureTime) {
                            // 트랜잭션 안이므로 아직 커밋 완료를 뜻하지 않는다.
                            log.warn(
                                    "[QUOTE][FUTURE_TIMESTAMP_REPAIR_ATTEMPT] "
                                            + "stock={} previousTime={} "
                                            + "receivedTime={} checkedAt={} jvmZone={}",
                                    safeCode(code),
                                    previousTime,
                                    received,
                                    checkedAt,
                                    ZoneId.systemDefault()
                            );
                        }

                        if (shouldUpdate) {
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

                /*
                 * transaction.execute가 정상 반환한 뒤 기록한다.
                 * 새 키움 요청에서 받은 값과 반환 시세 시각을 비교할 수 있다.
                 */
                if (force && result != null) {
                    log.info(
                            "[QUOTE][TRADE_QUOTE_RETURNED] "
                                    + "stock={} receivedTime={} fetchedAt={} nowUtc={}",
                            safeCode(code),
                            received,
                            result.fetchedAt(),
                            Instant.now()
                    );
                }

                return result;

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

    // 입력값의 개행 등으로 로그 구조가 깨지는 일을 막는다.
    private static String safeCode(String code) {
        if (code == null) {
            return "-";
        }

        String cleaned = code.replaceAll("[^A-Za-z0-9_-]", "?");
        return cleaned.substring(0, Math.min(cleaned.length(), 20));
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
     * 보유 종목과 거래대금 상위 종목을 수집 대상에 포함한다.
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