package com.myproject.stocksimulator.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

/**
 * Redis에서 Refresh Token의 유효 상태를 관리한다.
 *
 * Redis에는 토큰 원문 대신 SHA-256 해시를 저장한다.
 * 회원당 하나의 Refresh Token만 유지한다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;

    /**
     * 로그인 시 Refresh Token 저장
     * 동일 회원의 기존 토큰은 교체된다.
     */
    public void save(
            Long memberId,
            String token,
            Duration expiration
    ) {
        redisTemplate.opsForValue().set(
                key(memberId),
                hash(token),
                expiration
        );
    }

    /**
     * 기존 토큰이 일치할 때만 새로운 토큰으로 교체한다.
     *
     * Redis에서 비교와 교체를 하나의 작업으로 실행하여
     * 같은 Refresh Token의 중복 재발급을 방지한다.
     */
    public boolean rotate(
            Long memberId,
            String oldToken,
            String newToken,
            Duration expiration
    ) {
        String lua = """
                if redis.call('GET', KEYS[1]) == ARGV[1] then
                    redis.call('PSETEX', KEYS[1], ARGV[3], ARGV[2])
                    return 1
                end
                return 0
                """;

        DefaultRedisScript<Long> script =
                new DefaultRedisScript<>(lua, Long.class);

        Long result = redisTemplate.execute(
                script,
                List.of(key(memberId)),
                hash(oldToken),
                hash(newToken),
                String.valueOf(expiration.toMillis())
        );

        return Long.valueOf(1).equals(result);
    }

    /**
     * 전달받은 토큰이 Redis의 현재 토큰과 일치할 때만 삭제한다.
     */
    public boolean revoke(Long memberId, String token) {

        String lua = """
                if redis.call('GET', KEYS[1]) == ARGV[1] then
                    return redis.call('DEL', KEYS[1])
                end
                return 0
                """;

        DefaultRedisScript<Long> script =
                new DefaultRedisScript<>(lua, Long.class);

        Long result = redisTemplate.execute(
                script,
                List.of(key(memberId)),
                hash(token)
        );

        return Long.valueOf(1L).equals(result);
    }

    private String key(Long memberId) {
        return KEY_PREFIX + memberId;
    }

    /** 토큰 원문을 SHA-256 해시로 변환한다. */
    private String hash(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] bytes = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(bytes);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.",
                    e
            );
        }
    }
}