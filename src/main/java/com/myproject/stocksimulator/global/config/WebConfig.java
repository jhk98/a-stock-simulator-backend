package com.myproject.stocksimulator.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 관련 공통 설정 클래스
 *
 * React 프론트엔드에서 Spring Boot API에 접근할 수 있도록
 * CORS(Cross-Origin Resource Sharing) 정책을 설정한다.
 *
 * 허용할 프론트엔드 주소는 application.properties 또는
 * CORS_ALLOWED_ORIGINS 환경변수에서 관리한다.
 *
 * Spring Security 및 JWT 인증 환경에서 Authorization 헤더와 Refresh Token 쿠키를 사용할 수 있도록 구성한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.properties에서 허용할 프론트엔드 주소 목록을 가져온다.
    private final String[] allowedOrigins;

    public WebConfig(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
        // 쉼표로 구분된 문자열을 배열로 변환
        this.allowedOrigins = allowedOrigins.split("\\s*,\\s*");
    }

    /**
     * CORS 정책 등록
     *
     * @param registry CORS 설정 등록 객체
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        // 모든 URL 경로에 CORS 정책 사용
        registry.addMapping("/**")

                // 허용할 프론트엔드 주소
                .allowedOrigins(allowedOrigins)

                // 허용할 HTTP 요청 메서드
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )

                // 모든 요청 헤더 허용
                // Authorization: Bearer <JWT> 포함
                .allowedHeaders("*")

                // 브라우저에서 응답을 읽을 수 있도록 허용할 헤더
                .exposedHeaders("Authorization")

                // Refresh Token 쿠키를 사용하는 교차 출처 요청 허용
                .allowCredentials(true)

                // Preflight(OPTIONS) 요청 결과 캐시 시간: 1시간
                .maxAge(3600);
    }
}