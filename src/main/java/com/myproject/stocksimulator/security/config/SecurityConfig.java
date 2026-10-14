package com.myproject.stocksimulator.security.config;

import com.myproject.stocksimulator.security.handler.JwtAccessDeniedHandler;
import com.myproject.stocksimulator.security.handler.JwtAuthenticationEntryPoint;
import com.myproject.stocksimulator.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Spring Security 설정
 *
 * 주요 역할:
 * 1. JWT 인증 필터 등록
 * 2. 공개 API와 인증 필수 API 구분
 * 3. 서버 세션 비활성화
 * 4. CORS 및 CSRF 설정
 * 5. 401 / 403 오류 처리
 *
 * 로그인 세션 대신 JWT 기반 인증을 사용한다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    /**
     * JWT 필터의 중복 등록 방지
     *
     * JwtAuthenticationFilter는 @Component이므로
     * 일반 서블릿 필터로 자동 등록될 수 있다.
     *
     * 여기서는 자동 등록을 비활성화하고
     * Spring Security 필터 체인에서만 실행하도록 한다.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
    jwtFilterRegistration() {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(jwtAuthenticationFilter);

        registration.setEnabled(false);

        return registration;
    }

    /**
     * HTTP 요청에 적용할 보안 정책
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        // CSRF 토큰을 쿠키에 저장
        // 프론트엔드가 읽을 수 있도록 HttpOnly=false
        CookieCsrfTokenRepository csrfRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        // CSRF 토큰을 요청 헤더에서 읽는 처리기
        CsrfTokenRequestAttributeHandler csrfHandler =
                new CsrfTokenRequestAttributeHandler();

        http
                // 1. WebConfig의 CORS 설정 사용
                .cors(Customizer.withDefaults())

                // 2. Refresh / Logout에 CSRF 보호 적용
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(csrfHandler)
                        .requireCsrfProtectionMatcher(request -> {

                            // POST 요청만 검사
                            if (!"POST".equals(request.getMethod())) {
                                return false;
                            }

                            String path = request.getServletPath();

                            return "/auth/refresh".equals(path)
                                    || "/auth/logout".equals(path);
                        })
                )

                // 3. 서버 세션을 생성하지 않음
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // 4. API별 접근 권한
                .authorizeHttpRequests(auth -> auth

                        // CORS 사전 요청
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // CSRF 토큰 조회
                        .requestMatchers(
                                HttpMethod.GET,
                                "/auth/csrf"
                        ).permitAll()

                        // 로그인 / 재발급 / 로그아웃
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/logout"
                        ).permitAll()

                        // 회원가입
                        .requestMatchers(
                                HttpMethod.POST,
                                "/members"
                        ).permitAll()

                        // 회원가입 중복 확인
                        .requestMatchers(
                                HttpMethod.GET,
                                "/members/check-login-id",
                                "/members/check-email"
                        ).permitAll()

                        // 이메일 인증번호 발송 및 검증
                        .requestMatchers(
                                HttpMethod.POST,
                                "/emails/send",
                                "/emails/verify"
                        ).permitAll()

                        // 서버 상태 확인
                        .requestMatchers(
                                HttpMethod.GET,
                                "/actuator/health"
                        ).permitAll()

                        // 그 외 모든 요청은 JWT 인증 필요
                        .anyRequest().authenticated()
                )

                // 5. 기본 로그인 방식 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 6. 인증 및 접근 거부 오류 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 7. UsernamePasswordAuthenticationFilter보다
                //    먼저 JWT 인증 필터 실행
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}