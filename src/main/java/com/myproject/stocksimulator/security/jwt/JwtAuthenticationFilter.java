package com.myproject.stocksimulator.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 기반 사용자 인증 필터.
 *
 * <p>HTTP 요청의 Authorization 헤더에서 JWT를 추출하고,
 * 유효한 토큰이면 Spring Security에 인증 정보를 등록한다.</p>
 *
 * <p>OncePerRequestFilter를 상속하여
 * 하나의 요청에서 필터가 중복 실행되는 것을 방지한다.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Authorization 헤더 이름.
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * JWT 인증 방식 접두어.
     *
     * <p>Authorization: Bearer {JWT}</p>
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * JWT 생성 및 검증 객체.
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * HTTP 요청에서 JWT를 검증하고 인증 정보를 등록한다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 다음 필터 실행을 위한 객체
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            // 1. Authorization 헤더에서 JWT 추출
            String token = resolveToken(request);

            // 2. 토큰이 존재하고 유효한 Access Token인지 검증
            if (token != null && jwtTokenProvider.validateToken(token)) {

                // 3. JWT에서 회원 ID 추출
                Long memberId = jwtTokenProvider.getMemberId(token);

                // 4. 인증 객체 생성
                // 현재 단계에서는 회원 ID를 인증 주체로 사용한다.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                memberId,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                // 5. Spring Security에 인증 정보 등록
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

            // 6. 다음 필터 실행
            filterChain.doFilter(request, response);

        } finally {

            // 요청 처리가 끝난 뒤 인증 정보 정리
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Authorization 헤더에서 Bearer Token을 추출한다.
     *
     * @param request HTTP 요청
     * @return JWT 문자열 또는 null
     */
    private String resolveToken(HttpServletRequest request) {

        String bearerToken =
                request.getHeader(AUTHORIZATION_HEADER);

        // 헤더가 없거나 Bearer 방식이 아니면 null 반환
        if (bearerToken == null
                || !bearerToken.startsWith(BEARER_PREFIX)) {

            return null;
        }

        // "Bearer " 이후의 JWT 문자열만 반환
        return bearerToken.substring(BEARER_PREFIX.length());
    }
}