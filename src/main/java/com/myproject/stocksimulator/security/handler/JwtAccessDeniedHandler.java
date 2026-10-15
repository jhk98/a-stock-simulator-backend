package com.myproject.stocksimulator.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 403 Forbidden 처리
 *
 * <p>
 * 로그인은 되어 있지만 요청한 작업을 수행할
 * 권한이 없는 경우 실행된다.
 * </p>
 *
 * 403: 접근 권한 부족
 */
@Component
public class JwtAccessDeniedHandler
        implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.getWriter().write(
                "{\"message\":\"접근 권한이 없습니다.\"}"
        );
    }
}