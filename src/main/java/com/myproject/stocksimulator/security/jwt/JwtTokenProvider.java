package com.myproject.stocksimulator.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

/**
 * Access Token과 Refresh Token의 생성 및 검증을 담당한다.
 *
 * JWT에는 회원 ID, 토큰 종류, 고유 식별자만 저장한다.
 * 비밀번호나 주민등록번호 등 민감정보는 저장하지 않는다.
 */
@Component
public class JwtTokenProvider {

    public static final Duration ACCESS_EXPIRATION =
            Duration.ofMinutes(30);

    public static final Duration REFRESH_EXPIRATION =
            Duration.ofDays(7);

    private final SecretKey secretKey;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT 서명키는 최소 32바이트 이상이어야 합니다."
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /** Access Token 발급 */
    public String generateAccessToken(Long memberId) {
        return generateToken(
                memberId,
                "ACCESS",
                ACCESS_EXPIRATION
        );
    }

    /** Refresh Token 발급 */
    public String generateRefreshToken(Long memberId) {
        return generateToken(
                memberId,
                "REFRESH",
                REFRESH_EXPIRATION
        );
    }

    /** 공통 JWT 생성 */
    private String generateToken(
            Long memberId,
            String type,
            Duration expiration
    ) {
        Date now = new Date();

        return Jwts.builder()
                .subject(memberId.toString())
                .id(UUID.randomUUID().toString())
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(
                        now.getTime() + expiration.toMillis()
                ))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /** Access Token의 서명, 만료시간, 종류 검증 */
    public boolean validateAccessToken(String token) {
        return validateTokenType(token, "ACCESS");
    }

    /** Refresh Token의 서명, 만료시간, 종류 검증 */
    public boolean validateRefreshToken(String token) {
        return validateTokenType(token, "REFRESH");
    }

    private boolean validateTokenType(
            String token,
            String expectedType
    ) {
        try {
            Claims claims = getClaims(token);

            if (!expectedType.equals(
                    claims.get("type", String.class)
            )) {
                return false;
            }

            Long memberId = Long.parseLong(claims.getSubject());

            if (memberId <= 0) {
                return false;
            }

            return claims.getId() != null
                    && !claims.getId().isBlank();

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** 검증된 JWT에서 회원 ID 추출 */
    public Long getMemberId(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    /** JWT 서명 및 만료시간 검증 후 Claims 반환 */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}