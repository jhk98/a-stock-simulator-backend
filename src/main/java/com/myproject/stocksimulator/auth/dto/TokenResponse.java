package com.myproject.stocksimulator.auth.dto;

/**
 * Access Token 재발급 응답.
 */
public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}