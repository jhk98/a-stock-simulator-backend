package com.myproject.stocksimulator.login.controller;

import com.myproject.stocksimulator.login.dto.LoginRequest;
import com.myproject.stocksimulator.login.dto.LoginResponse;
import com.myproject.stocksimulator.login.dto.TokenResponse;
import com.myproject.stocksimulator.login.service.LoginService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * 로그인 및 인증 API Controller
 *
 * <p>
 * 주요 API:
 * POST /auth/login   - 로그인
 * POST /auth/refresh - Access Token 재발급
 * POST /auth/logout  - 로그아웃
 * GET  /auth/csrf    - CSRF 토큰 발급
 * </p>
 *
 * 패키지는 login이지만 기존 프론트엔드와의
 * 호환성을 위해 API 주소는 /auth를 유지한다.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    // HTTPS 환경에서는 true
    @Value("${app.auth.cookie-secure:true}")
    private boolean cookieSecure;

    // Refresh Token 쿠키 유효기간
    @Value("${jwt.refresh-expiration-seconds:604800}")
    private long refreshExpirationSeconds;

    /**
     * 로그인 API
     *
     * <p> Access Token은 JSON 응답으로 반환한다.
     * Refresh Token은 HttpOnly 쿠키에 저장한다. </p>
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {

        LoginService.LoginResult result =
                loginService.login(request);

        // Refresh Token을 쿠키로 전달
        addRefreshCookie(response, result.refreshToken());

        return ResponseEntity.ok(result.response());
    }

    /**
     * Access Token 재발급 API
     *
     * <p> 브라우저가 보낸 Refresh Token 쿠키를 검증하고
     * 새 Access Token과 Refresh Token을 발급한다. </p>
     *
     * 이 API는 CSRF 검증도 통과해야 한다.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(
                    name = "refreshToken",
                    required = false
            ) String refreshToken,
            HttpServletResponse response
    ) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException(
                    "Refresh Token이 없습니다."
            );
        }

        LoginService.RefreshResult result =
                loginService.refresh(refreshToken);

        // 새 Refresh Token으로 쿠키 갱신
        addRefreshCookie(response, result.refreshToken());

        return ResponseEntity.ok(result.response());
    }

    /**
     * 로그아웃 API
     *
     * <p>
     * 1. Redis의 Refresh Token 삭제
     * 2. 브라우저 Refresh Token 쿠키 만료
     * </p>
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    name = "refreshToken",
                    required = false
            ) String refreshToken,
            HttpServletResponse response
    ) {

        loginService.logout(refreshToken);

        // 브라우저의 Refresh Token 쿠키 제거
        ResponseCookie expiredCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/auth")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                expiredCookie.toString()
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * CSRF 토큰 발급 API
     *
     * <p> 프론트엔드는 이 API를 호출하여 XSRF-TOKEN 쿠키를 받고,
     * Refresh / Logout 요청 시 X-XSRF-TOKEN 헤더로 전달한다. </p>
     */
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {

        // 지연 생성된 CSRF 토큰을 실제로 생성하도록 접근
        csrfToken.getToken();

        return ResponseEntity.noContent().build();
    }

    /**
     * Refresh Token 쿠키 생성
     *
     * <p>
     * HttpOnly:
     * JavaScript에서 쿠키 내용을 읽을 수 없도록 한다.
     *
     * <p>
     * Secure:
     * true일 때 HTTPS 연결에서만 전송한다.
     * </p>
     *
     * <p>
     * SameSite=Lax:
     * 다른 사이트에서 발생한 일부 요청의 쿠키 전송을 제한한다.
     * </p>
     */
    private void addRefreshCookie(
            HttpServletResponse response,
            String token
    ) {
        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/auth")
                .maxAge(Duration.ofSeconds(refreshExpirationSeconds))
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }
}