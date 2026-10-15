package com.myproject.stocksimulator.login.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

/**
 * Refresh Token 저장소
 *
 * <p> Redis를 사용하여 Refresh Token을 관리한다. </p>
 *
 * 주요 기능
 * 1. Refresh Token 저장
 * 2. Refresh Token 일회성 소비
 * 3. 로그아웃 시 Refresh Token 삭제
 *
 * <p> Redis Key에는 Refresh Token 원문 대신 SHA-256 해시값을 사용한다. </p>
 *
 * Redis Value에는 회원 ID를 저장한다.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final StringRedisTemplate redisTemplate;

    /** Refresh Token 저장
     *
     * @param token     Refresh Token 원문
     * @param memberId  로그인한 회원 ID
     * @param ttl       토큰 유효기간
     */
    public void save(String token, Long memberId, Duration ttl) {
        redisTemplate.opsForValue().set(
                createKey(token),
                memberId.toString(),
                ttl
        );
    }

    /**
     * Refresh Token을 한 번만 사용하도록 소비한다.
     *
     * <p> Redis GETDEL을 사용하여 조회와 삭제를 하나의 명령으로 처리한다. </p>
     *
     * @return  회원 ID, 토큰이 없으면 null
     */
    public Long consume(String token) {
        String value = redisTemplate.opsForValue()
                .getAndDelete(createKey(token));

        return value == null ? null : Long.valueOf(value);
    }

    /**
     * Refresh Token 삭제
     *
     * <p> 로그아웃 시 사용한다. </p>
     */
    public void delete(String token) {
        redisTemplate.delete(createKey(token));
    }

    /**
     * Refresh Token으로 Redis Key 생성
     *
     * <p> 토큰 원문이 Redis Key에 노출되지 않도록 SHA-256 해시값으로 변환한다. </p>
     */
    private String createKey(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return "auth:refresh:" + HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Refresh Token 해시 생성에 실패했습니다.", e);
        }
    }
}