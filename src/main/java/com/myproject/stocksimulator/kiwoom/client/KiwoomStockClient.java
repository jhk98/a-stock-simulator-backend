package com.myproject.stocksimulator.kiwoom.client;

import com.myproject.stocksimulator.kiwoom.KiwoomStockInfo;
import com.myproject.stocksimulator.kiwoom.KiwoomStockListResponse;
import com.myproject.stocksimulator.kiwoom.KiwoomApiException;
import com.myproject.stocksimulator.kiwoom.KiwoomAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class KiwoomStockClient {

    private static final int MAX_PAGES = 20;

    private final KiwoomHttpClient httpClient;
    private final KiwoomAuthService authService;

    public KiwoomStockClient(
            KiwoomHttpClient httpClient,
            KiwoomAuthService authService
    ) {
        this.httpClient = httpClient;
        this.authService = authService;
    }

    public List<KiwoomStockInfo> getStockList(String marketType) {
        if (!"0".equals(marketType) && !"10".equals(marketType)) {
            throw new IllegalArgumentException("지원하지 않는 시장 구분입니다.");
        }

        List<KiwoomStockInfo> result = new ArrayList<>();
        Set<String> receivedKeys = new HashSet<>();

        String nextKey = null;

        for (int page = 0; page < MAX_PAGES; page++) {
            ResponseEntity<KiwoomStockListResponse> response =
                    requestPage(marketType, nextKey);

            KiwoomStockListResponse body = response.getBody();

            // HTTP 200이더라도 업무 응답 코드가 실패일 수 있습니다.
            // list가 누락된 응답도 정상 빈 목록으로 취급하지 않습니다.
            if (body == null
                    || body.returnCode() == null
                    || body.returnCode() != 0
                    || body.stocks() == null
                    || body.stocks().stream().anyMatch(item -> item == null)) {
                throw KiwoomApiException.invalidResponse();
            }

            result.addAll(body.stocks());

            String contYn = response.getHeaders().getFirst("cont-yn");

            if (contYn == null || "N".equals(contYn)) {
                return List.copyOf(result);
            }

            if (!"Y".equals(contYn)) {
                throw KiwoomApiException.invalidResponse();
            }

            nextKey = response.getHeaders().getFirst("next-key");

            // 키 누락이나 같은 연속조회 키 반복으로 인한 무한 호출을 방지합니다.
            if (nextKey == null
                    || nextKey.isBlank()
                    || !receivedKeys.add(nextKey)) {
                throw KiwoomApiException.invalidResponse();
            }
        }

        // 중간까지 받은 결과를 성공한 전체 목록처럼 반환하지 않습니다.
        throw new KiwoomApiException(0, "종목 연속조회 한도를 초과했습니다.");
    }

    private ResponseEntity<KiwoomStockListResponse> requestPage(
            String marketType,
            String nextKey
    ) {
        // HTTP 401일 때만 토큰을 교체하고 한 번 재시도합니다.
        // 429/서버 오류/알 수 없는 업무 오류를 무한 재시도하지 않습니다.
        for (int attempt = 0; attempt < 2; attempt++) {
            String token = authService.getAccessToken();

            try {
                return httpClient.post(
                        "/api/dostk/stkinfo",
                        "ka10099",
                        token,
                        nextKey,
                        Map.of("mrkt_tp", marketType),
                        KiwoomStockListResponse.class
                );

            } catch (KiwoomApiException e) {
                if (e.upstreamStatus() == 401) {
                    authService.invalidateIfSame(token);

                    if (attempt == 0) {
                        continue;
                    }
                }

                throw e;
            }
        }

        throw KiwoomApiException.invalidResponse();
    }
}