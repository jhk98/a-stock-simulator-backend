package com.myproject.stocksimulator.market.service;

import com.myproject.stocksimulator.kiwoom.KiwoomApiException;
import com.myproject.stocksimulator.kiwoom.KiwoomAuthService;
import com.myproject.stocksimulator.kiwoom.client.KiwoomHttpClient;
import com.myproject.stocksimulator.stock.entity.Stock;
import com.myproject.stocksimulator.stock.entity.StockType;
import com.myproject.stocksimulator.stock.repository.StockRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 종목명 검색, 시장 구분, 정렬, 거래대금 순위 조회
 *
 * DB 조회는 JPA로 처리한다.
 * 목록 가격은 마지막으로 저장된 시세를 사용한다.
 */
@Service
public class MarketService {

    private static final Logger log =
            LoggerFactory.getLogger(MarketService.class);

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final StockRepository stocks;
    private final KiwoomHttpClient http;
    private final KiwoomAuthService auth;

    @PersistenceContext
    private EntityManager entityManager;

    // 외부 API의 순위 응답을 기존처럼 메모리에 보관한다.
    private volatile Ranking ranking = new Ranking(
            List.of(),
            null,
            "거래대금 순위를 수집하고 있습니다."
    );

    public MarketService(
            StockRepository stocks,
            KiwoomHttpClient http,
            KiwoomAuthService auth
    ) {
        this.stocks = stocks;
        this.http = http;
        this.auth = auth;
    }

    public record StockRow(
            long id,
            String stockCode,
            String stockName,
            String stockType,
            Long currentPrice,
            BigDecimal changeRate,
            Long volume,
            Instant priceUpdatedAt
    ) {
    }

    public record StockPage(
            List<StockRow> items,
            int page,
            int size,
            long totalElements,
            long pricedElements,
            long totalPages,
            boolean hasNext
    ) {
    }

    public record PopularStock(
            long stockId,
            String stockCode,
            String stockName,
            String market,
            int rank,
            long currentPrice,
            BigDecimal changeRate,
            BigDecimal tradeValueMillion
    ) {
    }

    public record Ranking(
            List<PopularStock> items,
            Instant fetchedAt,
            String warning
    ) {
    }

    /**
     * 종목명만 검색한다.
     * 종목코드는 상세 조회와 주문 식별을 위해 응답에 유지한다.
     */
    @Transactional(readOnly = true)
    public StockPage search(
            String market,
            String keyword,
            String sort,
            int page,
            int size
    ) {
        if (page < 0 || size < 1 || size > 100
                || (long) page * size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "페이지 조건이 올바르지 않습니다."
            );
        }

        if (market == null
                || !Set.of("ALL", "KOSPI", "KOSDAQ").contains(market)) {
            throw new IllegalArgumentException(
                    "시장 구분이 올바르지 않습니다."
            );
        }

        String word = keyword == null ? "" : keyword.trim();

        if (word.length() > 100) {
            throw new IllegalArgumentException(
                    "검색어는 100자 이내로 입력해주세요."
            );
        }

        String selectedSort = sort == null || sort.isBlank()
                ? "PRICE_DESC"
                : sort;

        /*
         * 사용자 입력을 정렬 구문에 직접 넣지 않는다.
         * 같은 값은 종목코드로 추가 정렬한다.
         * 시세가 없는 종목은 숫자 정렬의 마지막에 둔다.
         */
        String orderBy = switch (selectedSort) {
            case "NAME_ASC" ->
                    "s.stockName asc, s.stockCode asc";
            case "PRICE_DESC" ->
                    """
                    case when p.currentPrice is null then 1 else 0 end,
                    p.currentPrice desc, s.stockCode asc
                    """;
            case "VOLUME_DESC" ->
                    """
                    case when p.currentVolume is null then 1 else 0 end,
                    p.currentVolume desc, s.stockCode asc
                    """;
            case "CHANGE_DESC" ->
                    """
                    case when p.changeRate is null then 1 else 0 end,
                    p.changeRate desc, s.stockCode asc
                    """;
            case "CHANGE_ASC" ->
                    """
                    case when p.changeRate is null then 1 else 0 end,
                    p.changeRate asc, s.stockCode asc
                    """;
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 정렬입니다."
            );
        };

        StringBuilder where = new StringBuilder(" where 1 = 1");
        Map<String, Object> parameters = new HashMap<>();

        if (!"ALL".equals(market)) {
            where.append(" and s.stockType = :market");
            parameters.put("market", StockType.valueOf(market));
        }

        if (!word.isEmpty()) {
            // %, _를 검색 와일드카드가 아닌 입력 문자로 취급한다.
            String pattern = "%"
                    + word.toLowerCase(Locale.ROOT)
                    .replace("!", "!!")
                    .replace("%", "!%")
                    .replace("_", "!_")
                    + "%";

            where.append(
                    " and lower(s.stockName) like :keyword escape '!'"
            );
            parameters.put("keyword", pattern);
        }

        String from = """
                from Stock s
                left join CurrentPrice p on p.stock = s
                """;

        TypedQuery<Object[]> countQuery = entityManager.createQuery(
                "select count(s), count(p.currentPrice) " + from + where,
                Object[].class
        );

        parameters.forEach(countQuery::setParameter);
        Object[] counts = countQuery.getSingleResult();

        long total = ((Number) counts[0]).longValue();
        long priced = ((Number) counts[1]).longValue();

        TypedQuery<Object[]> listQuery = entityManager.createQuery(
                """
                select s.id, s.stockCode, s.stockName, s.stockType,
                       p.currentPrice, p.changeRate,
                       p.currentVolume, p.updatedAt
                """ + from + where + " order by " + orderBy,
                Object[].class
        );

        parameters.forEach(listQuery::setParameter);

        List<StockRow> items = listQuery
                .setFirstResult(Math.toIntExact((long) page * size))
                .setMaxResults(size)
                .getResultList()
                .stream()
                .map(row -> {
                    LocalDateTime updated = (LocalDateTime) row[7];

                    return new StockRow(
                            ((Number) row[0]).longValue(),
                            (String) row[1],
                            (String) row[2],
                            ((StockType) row[3]).name(),
                            (Long) row[4],
                            (BigDecimal) row[5],
                            (Long) row[6],
                            updated == null
                                    ? null
                                    : updated.atZone(SEOUL).toInstant()
                    );
                })
                .toList();

        return new StockPage(
                items,
                page,
                size,
                total,
                priced,
                (total + size - 1) / size,
                ((long) page + 1) * size < total
        );
    }

    // 메인 화면과 시세 수집 대상 선정에서 사용한다.
    public Ranking ranking() {
        Ranking current = ranking;

        if (current.fetchedAt() != null
                && current.fetchedAt().isBefore(
                Instant.now().minusSeconds(180)
        )) {
            return new Ranking(
                    current.items(),
                    current.fetchedAt(),
                    "순위 갱신이 지연되고 있습니다. 수집 시각을 확인해주세요."
            );
        }

        return current;
    }

    /**
     * 외부 거래대금 순위 중 DB에 등록된 국내 종목만 남긴다.
     * 외부 API 대기 중 DB 트랜잭션을 유지하지 않는다.
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 60000)
    public void refreshRanking() {
        try {
            Map<String, Stock> registered = new HashMap<>();

            for (Stock stock : stocks.findAll()) {
                registered.put(stock.getStockCode(), stock);
            }

            Map<?, ?> body = fetchRanking();

            if (body == null
                    || !"0".equals(String.valueOf(body.get("return_code")))
                    || !(body.get("trde_prica_upper") instanceof List<?> rows)) {
                throw KiwoomApiException.invalidResponse();
            }

            List<PopularStock> items = new ArrayList<>();
            Set<String> seen = new HashSet<>();

            for (Object raw : rows) {
                if (!(raw instanceof Map<?, ?> row)) {
                    throw KiwoomApiException.invalidResponse();
                }

                String code = String.valueOf(row.get("stk_cd")).trim();
                Stock stock = registered.get(code);

                if (stock == null || !seen.add(code)) {
                    continue;
                }

                long price = decimal(row, "cur_prc").abs().longValueExact();
                int rank = decimal(row, "now_rank").intValueExact();
                BigDecimal value = decimal(row, "trde_prica");

                if (price <= 0 || rank <= 0 || value.signum() < 0) {
                    continue;
                }

                items.add(new PopularStock(
                        stock.getId(),
                        code,
                        stock.getStockName(),
                        stock.getStockType().name(),
                        rank,
                        price,
                        decimal(row, "flu_rt"),
                        value
                ));
            }

            List<PopularStock> selected = items.stream()
                    .sorted(Comparator.comparingInt(PopularStock::rank))
                    .limit(12)
                    .toList();

            ranking = new Ranking(
                    selected,
                    Instant.now(),
                    selected.isEmpty()
                            ? "현재 등록 종목과 일치하는 순위 데이터가 없습니다."
                            : ""
            );
        } catch (RuntimeException exception) {
            Ranking previous = ranking;

            ranking = new Ranking(
                    previous.items(),
                    previous.fetchedAt(),
                    "거래대금 순위 갱신에 실패했습니다."
            );

            log.warn(
                    "거래대금 순위 갱신 실패: {}",
                    exception.getClass().getSimpleName()
            );
        }
    }

    private Map<?, ?> fetchRanking() {
        for (int attempt = 0; attempt < 2; attempt++) {
            String token = auth.getAccessToken();

            try {
                return http.post(
                        "/api/dostk/rkinfo",
                        "ka10032",
                        token,
                        null,
                        Map.of(
                                "mrkt_tp", "000",
                                "mang_stk_incls", "0",
                                "stex_tp", "1"
                        ),
                        Map.class
                ).getBody();
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

    private BigDecimal decimal(Map<?, ?> row, String key) {
        Object value = row.get(key);

        if (value == null || value.toString().isBlank()) {
            throw KiwoomApiException.invalidResponse();
        }

        return new BigDecimal(value.toString().trim());
    }
}