package com.myproject.stocksimulator.login.dto;

/**
 * Access Token 재발급 응답 DTO
 *
 * <p> Refresh Token으로 새로운 Access Token을 발급받았을 때 사용한다. </p>
 */
public record TokenResponse(

        // 새로 발급한 Access Token
        String accessToken,

        // 인증 방식: Bearer
        String tokenType,

        // Access Token 유효기간 (초)
        long expiresIn
) {
}