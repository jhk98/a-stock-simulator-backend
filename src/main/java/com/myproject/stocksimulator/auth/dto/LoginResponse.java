package com.myproject.stocksimulator.auth.dto;

/**
 * 로그인 성공 응답 DTO
 *
 * <p> Access Token은 JSON 응답으로 전달한다.
 * Refresh Token은 별도의 HttpOnly 쿠키로 전달하므로 이 DTO에 포함하지 않는다. </p>
 */
public record LoginResponse(

    // API 요청에 사용할 JWT
    String accessToken,

    // Authorization 헤더에 사용할 인증 방식
    String tokenType,

    // Access Token 유효기간 (초)
    long expiresIn,

    Long memberId,

    String loginId,

    String name
) {
}