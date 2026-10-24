package com.myproject.stocksimulator.stock.service;

import com.myproject.stocksimulator.kiwoom.client.KiwoomHttpClient;
import com.myproject.stocksimulator.kiwoom.KiwoomApiException;
import com.myproject.stocksimulator.kiwoom.KiwoomAuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class DailyChartService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final KiwoomHttpClient http;
    private final KiwoomAuthService auth;
    private final QuoteService quotes;

    private final LinkedHashMap<String, ChartResponse> cache =
            new LinkedHashMap<>(64, 0.75f, true);

    private final Object[] locks = IntStream.range(0, 64)
            .mapToObj(index -> new Object())
            .toArray();

    public DailyChartService(
            KiwoomHttpClient http,
            KiwoomAuthService auth,
            QuoteService quotes
    ) {
        this.http = http;
        this.auth = auth;
        this.quotes = quotes;
    }

    public record Candle(
            LocalDate date,
            long open,
            long high,
            long low,
            long close,
            long volume
    ) {
    }

    public record ChartResponse(
            String stockCode,
            String stockName,
            boolean adjusted,
            Instant fetchedAt,
            List<Candle> candles
    ) {
    }

    public ChartResponse daily(String code) {
        QuoteService.StockInfo stock = quotes.stock(code);
        LocalDate today = LocalDate.now(SEOUL);
        String key = code + ":" + today;

        ChartResponse cached = cached(key);

        if (cached != null) {
            return cached;
        }

        synchronized (locks[Math.floorMod(code.hashCode(), locks.length)]) {
            cached = cached(key);

            if (cached != null) {
                return cached;
            }

            ChartResponse result = new ChartResponse(
                    code,
                    stock.stockName(),
                    false,
                    Instant.now(),
                    fetch(code, today)
            );

            // fetchedAt은 수집 완료 시각으로 기록한다.
            result = new ChartResponse(
                    result.stockCode(),
                    result.stockName(),
                    false,
                    Instant.now(),
                    result.candles()
            );

            synchronized (cache) {
                cache.put(key, result);

                while (cache.size() > 64) {
                    cache.remove(cache.keySet().iterator().next());
                }
            }

            return result;
        }
    }

    private ChartResponse cached(String key) {
        synchronized (cache) {
            ChartResponse value = cache.get(key);

            return value != null
                    && value.fetchedAt().isAfter(Instant.now().minusSeconds(120))
                    ? value
                    : null;
        }
    }

    private List<Candle> fetch(String code, LocalDate today) {
        TreeMap<LocalDate, Candle> values = new TreeMap<>();
        Set<String> receivedKeys = new HashSet<>();
        String nextKey = null;

        for (int page = 0; page < 5; page++) {
            ResponseEntity<Map> response = request(code, today, nextKey);
            Map<?, ?> body = response.getBody();

            if (
                    body == null
                            || !"0".equals(String.valueOf(body.get("return_code")))
                            || !code.equals(String.valueOf(body.get("stk_cd")).trim())
                            || !(body.get("stk_dt_pole_chart_qry") instanceof List<?> rows)
            ) {
                throw KiwoomApiException.invalidResponse();
            }

            for (Object raw : rows) {
                if (!(raw instanceof Map<?, ?> row)) {
                    throw KiwoomApiException.invalidResponse();
                }

                Candle candle = parse(row, today);

                if (candle != null) {
                    values.putIfAbsent(candle.date(), candle);
                }
            }

            String continuation = response.getHeaders().getFirst("cont-yn");

            if (
                    values.size() >= 180
                            || continuation == null
                            || "N".equals(continuation)
            ) {
                return values.descendingMap().values().stream()
                        .limit(180)
                        .sorted(Comparator.comparing(Candle::date))
                        .toList();
            }

            if (!"Y".equals(continuation)) {
                throw KiwoomApiException.invalidResponse();
            }

            nextKey = response.getHeaders().getFirst("next-key");

            if (
                    nextKey == null
                            || nextKey.isBlank()
                            || !receivedKeys.add(nextKey)
            ) {
                throw KiwoomApiException.invalidResponse();
            }
        }

        throw new KiwoomApiException(0, "차트 연속조회 한도를 초과했습니다.");
    }

    private Candle parse(Map<?, ?> row, LocalDate today) {
        try {
            LocalDate date = LocalDate.parse(
                    text(row, "dt"),
                    DateTimeFormatter.BASIC_ISO_DATE
            );

            long open = price(row, "open_pric");
            long high = price(row, "high_pric");
            long low = price(row, "low_pric");
            long close = price(row, "cur_prc");
            long volume = new BigDecimal(text(row, "trde_qty")).longValueExact();

            if (date.isAfter(today) || volume < 0) {
                throw KiwoomApiException.invalidResponse();
            }

            if (open == 0 && high == 0 && low == 0 && close == 0 && volume == 0) {
                return null;
            }

            if (
                    open <= 0 || high <= 0 || low <= 0 || close <= 0
                            || high < Math.max(open, close)
                            || low > Math.min(open, close)
                            || high < low
            ) {
                throw KiwoomApiException.invalidResponse();
            }

            return new Candle(date, open, high, low, close, volume);
        } catch (
                NumberFormatException
                | ArithmeticException
                | DateTimeException exception
        ) {
            throw KiwoomApiException.invalidResponse();
        }
    }

    private long price(Map<?, ?> row, String key) {
        return new BigDecimal(text(row, key)).abs().longValueExact();
    }

    private String text(Map<?, ?> row, String key) {
        Object value = row.get(key);

        if (value == null || value.toString().isBlank()) {
            throw KiwoomApiException.invalidResponse();
        }

        return value.toString().trim();
    }

    private ResponseEntity<Map> request(
            String code,
            LocalDate today,
            String nextKey
    ) {
        for (int attempt = 0; attempt < 2; attempt++) {
            String token = auth.getAccessToken();

            try {
                return http.post(
                        "/api/dostk/chart",
                        "ka10081",
                        token,
                        nextKey,
                        Map.of(
                                "stk_cd", code,
                                "base_dt", today.format(DateTimeFormatter.BASIC_ISO_DATE),
                                "upd_stkpc_tp", "0"
                        ),
                        Map.class
                );
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
}