package com.myproject.stocksimulator.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 401 Unauthorized 처리
 *
 * <p>
 * 인증이 필요한 API에 로그인하지 않고 접근하거나
 * 유효한 인증 정보가 없는 경우 실행된다.
 * </p>
 *
 * 401: 인증되지 않은 사용자
 */
@Component
public class JwtAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.getWriter().write(
                "{\"message\":\"인증이 필요합니다.\"}"
        );
    }
}