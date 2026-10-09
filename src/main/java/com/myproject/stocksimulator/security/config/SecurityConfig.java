package com.myproject.stocksimulator.security.config;

import com.myproject.stocksimulator.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
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

/**
 * Spring Security 보안 설정 클래스
 *
 * <p>애플리케이션의 HTTP 요청에 대한
 * 인증(Authentication) 및 인가(Authorization) 정책을 설정한다.</p>
 *
 * <p>JWT 기반 인증을 사용하므로 서버에 로그인 세션을 저장하지 않는
 * Stateless 방식으로 구성한다.</p>
 *
 * <p>JwtAuthenticationFilter를 Spring Security 필터 체인에 등록하여
 * HTTP 요청에 포함된 Access Token을 검증하도록 한다.</p>
 *
 * <p>현재는 기존 로그인 기능을 JWT 방식으로 전환하는 중이므로
 * 모든 API 요청을 임시로 허용한다.</p>
 *
 * <p>AuthController 및 AuthService 구현이 완료되면
 * 공개 API를 제외한 요청에는 인증을 요구하도록 변경해야 한다.</p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * JWT 인증 필터
     *
     * <p>HTTP 요청의 Authorization 헤더에서 JWT를 추출하고,
     * 유효한 토큰이면 Spring Security에 인증 정보를 등록한다.</p>
     *
     * <p>@RequiredArgsConstructor를 통해 생성자 주입된다.</p>
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Spring Security의 HTTP 보안 설정을 등록한다.
     *
     * @param http HTTP 보안 설정 객체
     * @return 설정이 완료된 SecurityFilterChain
     * @throws Exception 보안 설정 중 오류 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

                // ============================================================
                // 1. CORS 설정
                // ============================================================

                // global/config/WebConfig에 설정한 CORS 정책 사용
                .cors(Customizer.withDefaults())


                // ============================================================
                // 2. CSRF 설정
                // ============================================================

                // 현재는 JWT 기반 인증 구현 단계이므로 임시 비활성화
                //
                // Refresh Token을 쿠키로 전달하는 인증 기능을 구현할 때
                // CSRF 보호 정책을 별도로 적용해야 한다.
                .csrf(AbstractHttpConfigurer::disable)


                // ============================================================
                // 3. 세션 관리
                // ============================================================

                // 서버에서 로그인 세션을 생성하지 않는다.
                //
                // 각 HTTP 요청에 포함된 JWT를 이용하여
                // 사용자를 인증하는 Stateless 방식을 사용한다.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // ============================================================
                // 4. HTTP 요청 접근 권한
                // ============================================================

                .authorizeHttpRequests(auth -> auth

                        // 브라우저의 CORS 사전 요청 허용
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // 임시 설정
                        //
                        // 현재 기존 API 기능이 차단되지 않도록
                        // 모든 요청을 허용한다.
                        //
                        // JWT 로그인 구현이 완료되면
                        // 공개 API만 permitAll()로 설정하고
                        // 나머지는 authenticated()로 변경해야 한다.
                        .anyRequest().permitAll()
                )


                // ============================================================
                // 5. 기본 로그인 기능 비활성화
                // ============================================================

                // Spring Security 기본 로그인 페이지 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)


                // ============================================================
                // 6. JWT 인증 필터 등록
                // ============================================================

                // UsernamePasswordAuthenticationFilter보다 먼저
                // JwtAuthenticationFilter가 실행되도록 등록한다.
                //
                // 요청의 Authorization 헤더를 읽고
                // 유효한 Access Token이 있으면 인증 정보를 등록한다.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        // 설정한 보안 정책으로 SecurityFilterChain 생성
        return http.build();
    }
}
