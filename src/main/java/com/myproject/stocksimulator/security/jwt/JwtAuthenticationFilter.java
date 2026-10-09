package com.myproject.stocksimulator.security.jwt;

import com.myproject.stocksimulator.domain.member.Member;
import com.myproject.stocksimulator.domain.member.MemberStatus;
import com.myproject.stocksimulator.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * HTTP 요청에 포함된 JWT Access Token을 검증하는 필터
 *
 * 유효한 토큰이라도 회원이 탈퇴했거나 삭제되었다면
 * 인증 정보를 등록하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        // 유효한 Access Token인 경우에만 인증 처리
        if (token != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtTokenProvider.validateAccessToken(token)) {

            Long memberId = jwtTokenProvider.getMemberId(token);

            // 실제 DB에서 활성 회원 확인
            Member member = memberRepository
                    .findByIdAndStatus(
                            memberId,
                            MemberStatus.ACTIVE
                    )
                    .orElse(null);

            if (member != null && member.getRole() != null) {

                // DB에 저장된 실제 권한 사용
                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + member.getRole().name()
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                member.getId(),
                                null,
                                List.of(authority)
                        );

                // 요청에 사용할 인증 컨텍스트 생성
                SecurityContext context =
                        SecurityContextHolder.createEmptyContext();

                context.setAuthentication(authentication);

                SecurityContextHolder.setContext(context);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization: Bearer {token}
     * 형식에서 토큰 문자열만 추출한다.
     */
    private String resolveToken(HttpServletRequest request) {

        String authorization =
                request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token =
                authorization.substring(BEARER_PREFIX.length());

        return token.isBlank() ? null : token;
    }
}