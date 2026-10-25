package com.myproject.stocksimulator.kiwoom.client;

import com.myproject.stocksimulator.kiwoom.KiwoomApiException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class KiwoomHttpClient {

    private final RestClient restClient;
    private final long intervalNanos;

    private final Semaphore slots = new Semaphore(2, true);
    private final Object rateLock = new Object();

    private boolean started;
    private long lastStartedNanos;

    public KiwoomHttpClient(
            @Value("${kiwoom.api.base-url}") String baseUrl,
            @Value("${kiwoom.api.request-interval-ms:1100}") long intervalMs
    ) {
        if (!Set.of(
                "https://api.kiwoom.com",
                "https://mockapi.kiwoom.com"
        ).contains(baseUrl)) {
            log.error("[KiwoomHttpClient][ERROR][{}]", "키움 API 주소가 올바르지 않습니다.");
            throw new IllegalArgumentException("키움 API 주소가 올바르지 않습니다.");
        }

        if (intervalMs < 1 || intervalMs > 60_000) {
            log.error("[KiwoomHttpClient][ERROR][{}]", "키움 요청 간격이 올바르지 않습니다.");
            throw new IllegalArgumentException("키움 요청 간격이 올바르지 않습니다.");
        }

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(10));

        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();

        intervalNanos = TimeUnit.MILLISECONDS.toNanos(intervalMs);
    }

    public <T> ResponseEntity<T> post(
            String path,
            String apiId,
            String token,
            String nextKey,
            Object body,
            Class<T> responseType
    ) {
        if (!path.startsWith("/") || path.startsWith("//")) {
            log.error("[KiwoomHttpClient][post][ERROR][{}]", "상대 API 경로만 사용할 수 있습니다.");
            throw new IllegalArgumentException("상대 API 경로만 사용할 수 있습니다.");
        }

        boolean acquired = false;

        try {
            acquired = slots.tryAcquire(1500, TimeUnit.MILLISECONDS);

            if (!acquired) {
                log.error("[KiwoomHttpClient][post][ERROR][{}]", "키움 조회가 밀려 있습니다. 잠시 후 다시 시도해주세요.");
                throw new KiwoomApiException(
                        429,
                        "키움 조회가 밀려 있습니다. 잠시 후 다시 시도해주세요."
                );
            }

            awaitStart();

            var request = restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON);

            if (apiId != null) {
                request.header("api-id", apiId);
            }

            if (token != null) {
                request.headers(headers -> headers.setBearerAuth(token));
            }

            if (nextKey != null) {
                request.header("cont-yn", "Y");
                request.header("next-key", nextKey);
            }

            return request.body(body)
                    .retrieve()
                    .toEntity(responseType);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("[KiwoomHttpClient][post][ERROR][{}][{}]", "키움 요청 대기가 중단되었습니다.", exception.getMessage(), exception);
            throw new KiwoomApiException(0, "키움 요청 대기가 중단되었습니다.");
        } catch (RestClientResponseException exception) {
            log.error("[KiwoomHttpClient][post][ERROR][{}][{}]", "키움 HTTP 요청에 실패했습니다.", exception.getMessage(), exception);
            throw new KiwoomApiException(
                    exception.getStatusCode().value(),
                    "키움 HTTP 요청에 실패했습니다."
            );
        } catch (RestClientException exception) {
            log.error("[KiwoomHttpClient][post][ERROR][{}][{}]", "키움 통신에 실패했습니다.", exception.getMessage(), exception);
            throw new KiwoomApiException(0, "키움 통신에 실패했습니다.");
        } finally {
            if (acquired) {
                log.info("[KiwoomHttpClient][post][INFO][API 호출 완료][acquired: {}]", acquired);
                slots.release();
            }
        }
    }

    private void awaitStart() throws InterruptedException {
        synchronized (rateLock) {
            if (started) {
                long remaining = intervalNanos
                        - (System.nanoTime() - lastStartedNanos);

                if (remaining > 0) {
                    TimeUnit.NANOSECONDS.sleep(remaining);
                }
            }

            started = true;
            lastStartedNanos = System.nanoTime();
        }
    }
}