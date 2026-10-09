package com.myproject.stocksimulator.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * JWT 생성 및 검증을 담당하는 클래스
 *
 * <p>로그인에 성공한 회원에게 Access Token과 Refresh Token을 발급한다.</p>
 *
 * <p>JWT는 암호화된 데이터가 아니라 서명된 데이터이므로
 * 주민등록번호나 비밀번호 등의 민감정보를 포함하지 않는다.</p>
 *
 * <p>회원 식별을 위해 memberId를 JWT의 subject에 저장한다.</p>
 */
@Component
public class JwtTokenProvider {

    /**
     * Access Token 유효기간: 30분
     */
    private static final long ACCESS_TOKEN_EXPIRATION =
            Duration.ofMinutes(30).toMillis();

    /**
     * Refresh Token 유효기간: 7일
     */
    private static final long REFRESH_TOKEN_EXPIRATION =
            Duration.ofDays(7).toMillis();

    /**
     * JWT 서명 및 검증에 사용하는 비밀키
     */
    private final SecretKey secretKey;

    /**
     * JWT 서명키 초기화
     *
     * <p>application.properties에 정의된 jwt.secret 값을 주입받는다.</p>
     *
     * @param secret JWT 서명용 비밀키
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret
    ) {

        // HS256 알고리즘은 최소 256bit(32바이트) 키가 필요하다.
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT 비밀키는 최소 32바이트 이상이어야 합니다."
            );
        }

        // 문자열을 JWT 서명에 사용할 SecretKey 객체로 변환한다.
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성
     *
     * @param memberId 회원 ID
     * @return 생성된 JWT Access Token
     */
    public String generateAccessToken(Long memberId) {

        return generateToken(
                memberId,
                "ACCESS",
                ACCESS_TOKEN_EXPIRATION
        );
    }

    /**
     * Refresh Token 생성
     *
     * @param memberId 회원 ID
     * @return 생성된 JWT Refresh Token
     */
    public String generateRefreshToken(Long memberId) {

        return generateToken(
                memberId,
                "REFRESH",
                REFRESH_TOKEN_EXPIRATION
        );
    }

    /**
     * JWT를 생성하는 공통 메서드
     *
     * @param memberId 회원 ID
     * @param tokenType 토큰 종류 (ACCESS / REFRESH)
     * @param expiration 토큰 유효기간(ms)
     * @return 생성된 JWT
     */
    private String generateToken(
            Long memberId,
            String tokenType,
            long expiration
    ) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()

                // JWT의 주체: 회원 ID
                .subject(memberId.toString())

                // 토큰 종류 구분
                .claim("type", tokenType)

                // 토큰 발급 시각
                .issuedAt(now)

                // 토큰 만료 시각
                .expiration(expiryDate)

                // HS256 알고리즘으로 서명
                .signWith(secretKey, Jwts.SIG.HS256)

                // JWT 문자열 생성
                .compact();
    }

    /**
     * Access Token이 유효한지 검증
     *
     * <p>서명, 만료시간, 토큰 종류를 확인한다.</p>
     *
     * @param token JWT 문자열
     * @return 유효한 Access Token이면 true
     */
    public boolean validateToken(String token) {

        try {

            Claims claims = getClaims(token);

            return "ACCESS".equals(claims.get("type", String.class));

        } catch (JwtException | IllegalArgumentException e) {

            // 서명 오류, 만료, 잘못된 형식 등의 경우
            return false;
        }
    }

    /**
     * JWT에서 회원 ID를 추출
     *
     * <p>토큰의 서명과 만료시간을 검증한 후 subject를 읽는다.</p>
     *
     * @param token JWT 문자열
     * @return 회원 ID
     */
    public Long getMemberId(String token) {

        Claims claims = getClaims(token);

        return Long.valueOf(claims.getSubject());
    }

    /**
     * JWT의 Claims를 추출
     *
     * <p>Claims는 JWT에 저장된 데이터 집합이다.</p>
     *
     * <p>서명이 올바르지 않거나 만료된 토큰이면 예외가 발생한다.</p>
     *
     * @param token JWT 문자열
     * @return 검증된 JWT Claims
     */
    private Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}