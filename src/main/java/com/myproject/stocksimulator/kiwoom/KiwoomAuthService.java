package com.myproject.stocksimulator.kiwoom;

import com.myproject.stocksimulator.kiwoom.client.KiwoomAuthClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

@Service
public class KiwoomAuthService {

    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("uuuuMMddHHmmss")
                    .withResolverStyle(ResolverStyle.STRICT);

    private final KiwoomAuthClient authClient;

    private String accessToken;
    private Instant expiresAt;
    private Instant retryAfter = Instant.EPOCH;

    public KiwoomAuthService(KiwoomAuthClient authClient) {
        this.authClient = authClient;
    }

    public synchronized String getAccessToken() {
        Instant now = Instant.now();

        // 만료 1분 전까지는 기존 토큰을 재사용합니다.
        if (accessToken != null
                && expiresAt != null
                && now.isBefore(expiresAt.minusSeconds(60))) {
            return accessToken;
        }

        // 발급 실패 직후 요청이 몰려도 발급 API를 계속 호출하지 않습니다.
        if (now.isBefore(retryAfter)) {
            throw new KiwoomApiException(
                    0,
                    "키움 토큰 발급 재시도 대기 중입니다."
            );
        }

        try {
            KiwoomTokenResponse response = authClient.issueToken();

            if (response == null
                    || response.returnCode() == null
                    || response.returnCode() != 0
                    || response.token() == null
                    || response.token().isBlank()
                    || !"bearer".equalsIgnoreCase(response.tokenType())
                    || response.expiresDt() == null) {
                throw KiwoomApiException.invalidResponse();
            }

            // EC2/Docker 기본 시간대와 무관하게 한국 시각으로 해석합니다.
            Instant parsedExpiresAt = LocalDateTime.parse(
                    response.expiresDt(),
                    FORMATTER
            ).atZone(KOREA).toInstant();

            if (!Instant.now().plusSeconds(60).isBefore(parsedExpiresAt)) {
                throw KiwoomApiException.invalidResponse();
            }

            // 모든 검증이 끝난 다음 캐시를 교체합니다.
            accessToken = response.token();
            expiresAt = parsedExpiresAt;
            retryAfter = Instant.EPOCH;

            return accessToken;

        } catch (RuntimeException e) {
            accessToken = null;
            expiresAt = null;
            retryAfter = Instant.now().plusSeconds(10);

            if (e instanceof KiwoomApiException exception) {
                throw exception;
            }

            throw KiwoomApiException.invalidResponse();
        }
    }

    // 실패한 요청에서 사용한 토큰이 현재 캐시와 같을 때만 무효화합니다.
    // 이미 새 토큰이 발급됐다면 그 새 토큰은 지우지 않습니다.
    public synchronized void invalidateIfSame(String failedToken) {
        if (failedToken != null && failedToken.equals(accessToken)) {
            accessToken = null;
            expiresAt = null;
        }
    }

    // 기존 테스트에서 사용하는 메서드 형태를 유지합니다.
    public synchronized LocalDateTime getExpiresAt() {
        return expiresAt == null
                ? null
                : LocalDateTime.ofInstant(expiresAt, KOREA);
    }
}