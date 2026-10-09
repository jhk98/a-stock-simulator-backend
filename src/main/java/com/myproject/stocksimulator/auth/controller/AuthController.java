package com.myproject.stocksimulator.auth.controller;

import com.myproject.stocksimulator.auth.dto.LoginRequest;
import com.myproject.stocksimulator.auth.dto.LoginResponse;
import com.myproject.stocksimulator.auth.dto.TokenResponse;
import com.myproject.stocksimulator.auth.service.AuthService;
import com.myproject.stocksimulator.security.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

/**
 * 로그인, 토큰 재발급, 로그아웃 HTTP 요청을 처리한다.
 *
 * Access Token은 JSON 응답으로 전달하고,
 * Refresh Token은 HttpOnly 쿠키로 전달한다.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.auth.cookie-secure:true}")
    private boolean cookieSecure;

    /**
     * CSRF 토큰 발급
     *
     * 프론트엔드는 반환된 토큰을
     * X-XSRF-TOKEN 헤더에 포함하여 요청한다.
     */
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {

        return Map.of(
                "headerName", csrfToken.getHeaderName(),
                "token", csrfToken.getToken()
        );
    }

    /**
     * 로그인
     *
     * 1. 아이디 및 비밀번호 검증
     * 2. Access / Refresh Token 발급
     * 3. Refresh Token을 Redis에 저장
     * 4. Refresh Token을 HttpOnly 쿠키로 전달
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {

        AuthService.LoginResult result =
                authService.login(request);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie(
                                result.refreshToken(),
                                JwtTokenProvider.REFRESH_EXPIRATION
                        ).toString()
                )
                .body(result.response());
    }

    /**
     * Access Token 재발급
     *
     * Refresh Token을 검증하고 Redis에서 교체한다.
     * 기존 Refresh Token은 재사용할 수 없다.
     *
     * CSRF 검증은 SecurityConfig에서 수행한다.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(
                    name = "refreshToken",
                    required = false
            ) String refreshToken
    ) {

        AuthService.RefreshResult result =
                authService.refresh(refreshToken);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie(
                                result.refreshToken(),
                                JwtTokenProvider.REFRESH_EXPIRATION
                        ).toString()
                )
                .body(result.response());
    }

    /**
     * 로그아웃
     *
     * Redis에 저장된 Refresh Token을 폐기하고
     * 브라우저의 Refresh Token 쿠키를 삭제한다.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    name = "refreshToken",
                    required = false
            ) String refreshToken
    ) {

        authService.logout(refreshToken);

        return ResponseEntity.noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie(
                                "",
                                Duration.ZERO
                        ).toString()
                )
                .build();
    }

    /**
     * 인증 실패 응답
     *
     * 로그인 및 Refresh Token 검증 실패 시
     * HTTP 401을 반환한다.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException exception
    ) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "status", 401,
                        "code", "UNAUTHORIZED",
                        "message", exception.getMessage()
                ));
    }

    /**
     * Refresh Token 쿠키 생성
     *
     * HttpOnly : JavaScript 접근 차단
     * Secure   : HTTPS 환경에서만 쿠키 전송
     * SameSite : 교차 사이트 쿠키 전송 제한
     * Path     : /auth 경로에서만 쿠키 전송
     */
    private ResponseCookie refreshCookie(
            String value,
            Duration maxAge
    ) {

        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/auth")
                .maxAge(maxAge)
                .build();
    }
}