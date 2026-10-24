package com.myproject.stocksimulator.kiwoom.client;

import com.myproject.stocksimulator.kiwoom.KiwoomTokenRequest;
import com.myproject.stocksimulator.kiwoom.KiwoomTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KiwoomAuthClient {

    private final KiwoomHttpClient httpClient;
    private final String appKey;
    private final String secretKey;

    public KiwoomAuthClient(
            KiwoomHttpClient httpClient,
            @Value("${kiwoom.api.app-key}") String appKey,
            @Value("${kiwoom.api.secret-key}") String secretKey
    ) {
        if (appKey.isBlank() || secretKey.isBlank()) {
            throw new IllegalArgumentException("키움 인증 정보가 비어 있습니다.");
        }

        this.httpClient = httpClient;
        this.appKey = appKey;
        this.secretKey = secretKey;
    }

    // 이 클래스는 발급 요청만 담당합니다.
    // 토큰 재사용과 만료 판단은 KiwoomAuthService에서 수행합니다.
    public KiwoomTokenResponse issueToken() {
        return httpClient.post(
                "/oauth2/token",
                null,
                null,
                null,
                new KiwoomTokenRequest(
                        "client_credentials",
                        appKey,
                        secretKey
                ),
                KiwoomTokenResponse.class
        ).getBody();
    }
}