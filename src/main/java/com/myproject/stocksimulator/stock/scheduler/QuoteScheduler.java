package com.myproject.stocksimulator.stock.scheduler;

import com.myproject.stocksimulator.kiwoom.KiwoomApiException;
import com.myproject.stocksimulator.stock.service.QuoteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 기존 시세 자동 수집 작업
 * 한 번에 한 종목을 처리하며 실패한 종목은 다음 순환에서 다시 시도한다.
 */
@Component
public class QuoteScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(QuoteScheduler.class);

    private final QuoteService quotes;
    private int cursor;

    public QuoteScheduler(QuoteService quotes) {
        this.quotes = quotes;
    }

    @Scheduled(
            initialDelayString = "${simulation.quote.initial-delay-ms:30000}",
            fixedDelayString = "${simulation.quote.delay-ms:2500}"
    )
    public void refreshOne() {
        String stage = "TRACKED_CODES";
        String stockCode = "-";

        try {
            List<String> codes = quotes.trackedCodes();

            if (codes.isEmpty()) return;

            cursor = Math.floorMod(cursor, codes.size());
            stockCode = codes.get(cursor);
            cursor = (cursor + 1) % codes.size();

            stage = "QUOTE";
            quotes.quote(stockCode, false);
        } catch (RuntimeException exception) {
            String category = exception instanceof KiwoomApiException
                    ? "UPSTREAM"
                    : exception instanceof DataAccessException
                    ? "DATABASE"
                    : "INTERNAL";

            int upstreamStatus = exception instanceof KiwoomApiException api
                    ? api.upstreamStatus()
                    : 0;

            // 종목코드는 진단 로그에서만 사용한다.
            String safeCode = stockCode.replaceAll(
                    "[^A-Za-z0-9_-]",
                    "?"
            );

            log.warn(
                    "시세 수집 실패: stage={} stock={} category={} status={} type={}",
                    stage,
                    safeCode,
                    category,
                    upstreamStatus,
                    exception.getClass().getSimpleName()
            );
        }
    }
}