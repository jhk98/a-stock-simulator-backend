package com.myproject.stocksimulator.stock.scheduler;

import com.myproject.stocksimulator.kiwoom.KiwoomApiException;
import com.myproject.stocksimulator.market.service.MarketService.StockRow;
import com.myproject.stocksimulator.stock.service.QuoteService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 종목 시세 자동 수집
 *
 * 현재 목록에 보이는 종목, 보유 종목, 미수집 종목,
 * 오래된 시세를 가진 종목을 번갈아 처리한다.
 *
 * 목록 요청에서는 수집 대상을 등록하기만 한다.
 * 외부 API 통신은 이 작업에서 수행한다.
 */
@Component
public class QuoteScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(QuoteScheduler.class);

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // 목록 표시용 시세는 수집 후 60초 동안 재수집을 생략한다.
    // 주문 체결용 강제 시세 조회는 기존 QuoteService가 별도로 처리한다.
    private static final long DISPLAY_FRESH_SECONDS = 60;

    // 특정 종목 수집 실패 시 다른 종목을 먼저 처리한다.
    private static final long FAILURE_RETRY_SECONDS = 300;

    // 화면 이동을 반복해도 우선 수집 대기열이 무제한 커지지 않게 한다.
    private static final int MAX_VISIBLE_QUEUE = 512;

    private final QuoteService quotes;
    private final TransactionTemplate transaction;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * HTTP 요청 스레드와 수집 스레드가 함께 접근한다.
     * 같은 종목은 한 번만 대기열에 들어간다.
     */
    private final LinkedHashSet<String> visibleQueue = new LinkedHashSet<>();

    // 아래 상태는 단일 시세 수집 스레드에서만 접근한다.
    private final Map<String, Instant> retryAfter = new HashMap<>();

    private List<String> trackedCodes = List.of();
    private Instant trackedReloadAt = Instant.EPOCH;
    private int trackedCursor;

    private long missingCursor;
    private long allCursor;
    private int turn;

    private Instant pausedUntil = Instant.EPOCH;

    public QuoteScheduler(
            QuoteService quotes,
            PlatformTransactionManager transactionManager
    ) {
        this.quotes = quotes;
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setReadOnly(true);
    }

    /**
     * 목록 API에서 호출한다.
     *
     * 외부 API나 DB를 호출하지 않고 대기열에 등록만 하므로
     * 종목 목록 응답을 시세 수집 때문에 지연시키지 않는다.
     */
    public void prioritize(List<StockRow> rows) {
        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(DISPLAY_FRESH_SECONDS);

        synchronized (visibleQueue) {
            for (StockRow row : rows) {
                boolean usable = row.currentPrice() != null
                        && row.currentPrice() > 0
                        && row.priceUpdatedAt() != null
                        && row.priceUpdatedAt().isAfter(cutoff)
                        && !row.priceUpdatedAt().isAfter(now.plusSeconds(5));

                if (usable || visibleQueue.contains(row.stockCode())) {
                    continue;
                }

                if (visibleQueue.size() >= MAX_VISIBLE_QUEUE) {
                    break;
                }

                visibleQueue.add(row.stockCode());
            }
        }
    }

    /**
     * 한 번에 한 종목만 처리한다.
     *
     * 기존 2.5초 추가 대기를 줄이되,
     * 실제 외부 요청 간격은 KiwoomHttpClient의 제한을 유지한다.
     */
    @Scheduled(
            scheduler = "quoteTaskScheduler",
            initialDelayString =
                    "${simulation.quote.worker-initial-delay-ms:5000}",
            fixedDelayString =
                    "${simulation.quote.worker-delay-ms:200}"
    )
    public void refreshOne() {
        Instant now = Instant.now();

        if (now.isBefore(pausedUntil)) {
            return;
        }

        String stockCode = null;

        try {
            // 만료된 재시도 제한을 정리한다.
            retryAfter.entrySet().removeIf(
                    entry -> !entry.getValue().isAfter(now)
            );

            reloadTrackedCodes(now);

            int selectedTurn = turn;
            turn = (turn + 1) % 4;

            stockCode = switch (selectedTurn) {
                case 0 -> nextVisible(now);
                case 1 -> nextTracked(now);
                case 2 -> nextDatabaseStock(true, now);
                default -> nextDatabaseStock(false, now);
            };

            /*
             * 해당 종류의 대상이 없으면 다른 수집 대상을 처리한다.
             * 미수집 종목이 남아 있으면 빈 실행 기회를 활용한다.
             */
            if (stockCode == null) {
                stockCode = nextVisible(now);
            }

            if (stockCode == null) {
                stockCode = nextDatabaseStock(true, now);
            }

            if (stockCode == null) {
                stockCode = nextDatabaseStock(false, now);
            }

            if (stockCode == null) {
                return;
            }

            QuoteService.Quote saved = quotes.saved(stockCode);

            // 다른 요청이 이미 시세를 갱신했다면 외부 호출을 생략한다.
            if (QuoteService.fresh(saved, DISPLAY_FRESH_SECONDS)) {
                retryAfter.put(
                        stockCode,
                        saved.fetchedAt().plusSeconds(DISPLAY_FRESH_SECONDS)
                );
                return;
            }

            // 저장·검증 로직은 기존 시세 서비스를 그대로 사용한다.
            quotes.quote(stockCode, false);

            retryAfter.put(
                    stockCode,
                    Instant.now().plusSeconds(DISPLAY_FRESH_SECONDS)
            );
        } catch (RuntimeException exception) {
            Instant failedAt = Instant.now();

            if (stockCode != null) {
                retryAfter.put(
                        stockCode,
                        failedAt.plusSeconds(FAILURE_RETRY_SECONDS)
                );
            }

            int upstreamStatus = exception instanceof KiwoomApiException api
                    ? api.upstreamStatus()
                    : 0;

            /*
             * 요청이 밀리거나 호출 제한에 걸렸을 때
             * 계속 새 요청을 보내는 일을 피한다.
             */
            if (upstreamStatus == 429) {
                pausedUntil = failedAt.plusSeconds(30);
            } else if (exception instanceof DataAccessException) {
                pausedUntil = failedAt.plusSeconds(5);
            }

            String category = exception instanceof KiwoomApiException
                    ? "UPSTREAM"
                    : exception instanceof DataAccessException
                    ? "DATABASE"
                    : "INTERNAL";

            String safeCode = stockCode == null
                    ? "-"
                    : stockCode.replaceAll("[^A-Za-z0-9_-]", "?");

            log.warn(
                    "자동 시세 수집 실패: stock={} category={} status={} type={}",
                    safeCode,
                    category,
                    upstreamStatus,
                    exception.getClass().getSimpleName()
            );
        }
    }

    /**
     * 보유 종목과 주요 종목 목록은 매 실행마다 다시 조회하지 않는다.
     */
    private void reloadTrackedCodes(Instant now) {
        if (now.isBefore(trackedReloadAt)) {
            return;
        }

        trackedCodes = quotes.trackedCodes();
        trackedReloadAt = now.plusSeconds(30);

        if (trackedCodes.isEmpty()) {
            trackedCursor = 0;
        } else {
            trackedCursor %= trackedCodes.size();
        }
    }

    private String nextVisible(Instant now) {
        synchronized (visibleQueue) {
            Iterator<String> iterator = visibleQueue.iterator();

            while (iterator.hasNext()) {
                String code = iterator.next();
                iterator.remove();

                if (eligible(code, now)) {
                    return code;
                }
            }
        }

        return null;
    }

    private String nextTracked(Instant now) {
        for (int checked = 0; checked < trackedCodes.size(); checked++) {
            String code = trackedCodes.get(trackedCursor);

            trackedCursor = (trackedCursor + 1) % trackedCodes.size();

            if (eligible(code, now)) {
                return code;
            }
        }

        return null;
    }

    /**
     * ID 커서로 순회한다.
     *
     * 실패한 일부 종목이 항상 앞에 있다는 이유로
     * 뒤의 종목들이 영원히 수집되지 않는 문제를 피한다.
     */
    private String nextDatabaseStock(boolean missingOnly, Instant now) {
        long cursor = missingOnly ? missingCursor : allCursor;

        List<Object[]> rows = transaction.execute(status -> {
            String condition = missingOnly
                    ? """
                      and (
                          p.id is null
                          or p.currentPrice <= 0
                      )
                      """
                    : """
                      and (
                          p.id is null
                          or p.currentPrice <= 0
                          or p.updatedAt < :cutoff
                          or p.updatedAt > :future
                      )
                      """;

            TypedQuery<Object[]> query = entityManager.createQuery(
                    """
                    select s.id, s.stockCode
                    from Stock s
                    left join CurrentPrice p on p.stock = s
                    where s.id > :cursor
                    """ + condition + " order by s.id",
                    Object[].class
            );

            query.setParameter("cursor", cursor);

            if (!missingOnly) {
                query.setParameter(
                        "cutoff",
                        LocalDateTime.ofInstant(
                                now.minusSeconds(DISPLAY_FRESH_SECONDS),
                                SEOUL
                        )
                );

                query.setParameter(
                        "future",
                        LocalDateTime.ofInstant(now.plusSeconds(5), SEOUL)
                );
            }

            return query.setMaxResults(32).getResultList();
        });

        if (rows == null || rows.isEmpty()) {
            setCursor(missingOnly, 0);
            return null;
        }

        for (Object[] row : rows) {
            long id = ((Number) row[0]).longValue();
            String code = (String) row[1];

            setCursor(missingOnly, id);

            if (eligible(code, now)) {
                return code;
            }
        }

        return null;
    }

    private void setCursor(boolean missingOnly, long value) {
        if (missingOnly) {
            missingCursor = value;
        } else {
            allCursor = value;
        }
    }

    private boolean eligible(String code, Instant now) {
        Instant availableAt = retryAfter.get(code);

        return availableAt == null || !availableAt.isAfter(now);
    }
}