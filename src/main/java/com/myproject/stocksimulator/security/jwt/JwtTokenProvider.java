package com.myproject.stocksimulator.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 생성 및 검증 클래스
 *
 * 주요 기능:
 * 1. Access Token 생성
 * 2. JWT 서명 및 만료시간 검증
 * 3. JWT에서 회원 ID 추출
 *
 * JWT Payload의 subject에는 회원 ID를 저장한다.
 *
 * 주의:
 * JWT는 암호화가 아니라 서명 기반 토큰이다.
 * 비밀번호 등 민감한 개인정보를 Payload에 넣지 않는다.
 */
@Component
public class JwtTokenProvider {

    // JWT 서명 및 검증에 사용할 비밀키
    private final SecretKey secretKey;

    // Access Token 유효기간 (초)
    private final long accessExpirationSeconds;

    /**
     * application.properties에서 JWT 설정을 주입받는다.
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-seconds:900}")
            long accessExpirationSeconds
    ) {

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // HMAC-SHA256에 필요한 최소 키 길이 검사
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT 비밀키는 최소 32바이트 이상이어야 합니다."
            );
        }

        if (accessExpirationSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Access Token 유효기간은 0보다 커야 합니다."
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpirationSeconds = accessExpirationSeconds;
    }

    /**
     * Access Token 생성
     *
     * subject: 회원 ID
     * issuedAt: 토큰 발급 시간
     * expiration: 토큰 만료 시간
     */
    public String createAccessToken(Long memberId) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(
                        now.plusSeconds(accessExpirationSeconds)
                ))
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT 서명과 만료시간을 검증한 뒤 회원 ID 반환
     *
     * 유효하지 않은 JWT는 예외가 발생한다.
     */
    public Long getMemberId(String token) throws JwtException {

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }

    /**
     * Access Token 유효기간 반환
     */
    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }
}