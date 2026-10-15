package com.myproject.stocksimulator.security.jwt;

import com.myproject.stocksimulator.repository.MemberRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터
 *
 * <p>
 * 클라이언트의 HTTP 요청에서 Access Token을 추출하고
 * 유효한 토큰이면 Spring Security에 인증 정보를 등록한다.
 * </p>
 *
 * 처리 순서:
 * 1. Authorization 헤더 확인
 * 2. Bearer 토큰 추출
 * 3. JWT 서명 및 만료 검증
 * 4. 회원 존재 여부 확인
 * 5. SecurityContext에 인증 정보 저장
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Authorization 헤더 조회
        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        // 2. Bearer 토큰이 없으면 다음 필터로 이동
        //    공개 API도 이 필터를 통과해야 하기 때문이다.
        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 3. "Bearer " 이후의 JWT 추출
        String token = authorization.substring(7);

        try {

            // 4. JWT 검증 및 회원 ID 추출
            Long memberId = jwtTokenProvider.getMemberId(token);

            // 5. 회원이 실제로 존재하는지 확인
            if (memberRepository.existsById(memberId)) {

                // Spring Security 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                memberId,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_USER"
                                        )
                                )
                        );

                // 인증 성공 상태 저장
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (RuntimeException e) {

            // JWT가 잘못되었거나 만료된 경우
            // 인증 정보를 제거한다.
            SecurityContextHolder.clearContext();
        }

        // 6. 다음 Spring Security 필터 실행
        filterChain.doFilter(request, response);
    }
}