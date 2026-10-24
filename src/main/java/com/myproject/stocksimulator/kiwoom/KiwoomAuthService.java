package com.myproject.stocksimulator.kiwoom;

import com.myproject.stocksimulator.kiwoom.client.KiwoomAuthClient;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * 키움 API 접근 토큰 발급과 재사용
 */
@Service
public class KiwoomAuthService {

    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("uuuuMMddHHmmss")
                    .withResolverStyle(ResolverStyle.STRICT);

    private final KiwoomAuthClient authClient;

    private String accessToken;

    // 외부 조회용 getter는 없어도 내부 만료 검사에는 필요한 값이다.
    private Instant expiresAt;

    private Instant retryAfter = Instant.EPOCH;

    public KiwoomAuthService(KiwoomAuthClient authClient) {
        this.authClient = authClient;
    }

    /**
     * 만료가 임박하지 않았다면 기존 토큰을 반환한다.
     * 동시에 여러 요청이 들어와도 토큰 발급을 직렬화한다.
     */
    public synchronized String getAccessToken() {
        Instant now = Instant.now();

        if (accessToken != null
                && expiresAt != null
                && now.isBefore(expiresAt.minusSeconds(60))) {
            return accessToken;
        }

        // 발급 실패 직후 같은 요청이 반복되는 것을 제한한다.
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

            // 서버 기본 시간대와 관계없이 한국 시각으로 해석한다.
            Instant parsedExpiresAt = LocalDateTime.parse(
                    response.expiresDt(),
                    FORMATTER
            ).atZone(KOREA).toInstant();

            if (!Instant.now().plusSeconds(60).isBefore(parsedExpiresAt)) {
                throw KiwoomApiException.invalidResponse();
            }

            // 응답 검증이 끝난 뒤 토큰 캐시를 변경한다.
            accessToken = response.token();
            expiresAt = parsedExpiresAt;
            retryAfter = Instant.EPOCH;

            return accessToken;
        } catch (RuntimeException exception) {
            accessToken = null;
            expiresAt = null;
            retryAfter = Instant.now().plusSeconds(10);

            if (exception instanceof KiwoomApiException apiException) {
                throw apiException;
            }

            throw KiwoomApiException.invalidResponse();
        }
    }

    /**
     * 실패한 요청에서 사용한 토큰이 현재 토큰과 같을 때만 제거한다.
     * 다른 요청이 이미 새로 발급한 토큰을 지우지 않는다.
     */
    public synchronized void invalidateIfSame(String failedToken) {
        if (failedToken != null && failedToken.equals(accessToken)) {
            accessToken = null;
            expiresAt = null;
        }
    }
}