package com.myproject.stocksimulator.auth.dto;

/**
 * 로그인 성공 응답
 *
 * Refresh Token은 응답 본문에 포함하지 않고
 * HttpOnly 쿠키로 전달한다.
 */
public record LoginResponse(
        Long memberId,
        String loginId,
        String name,
        String accessToken,
        String tokenType,
        long expiresIn
) {
}