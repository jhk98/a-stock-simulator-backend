package com.myproject.stocksimulator.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 암호화 설정
 *
 * <p> BCrypt 알고리즘을 사용한다. </p>
 *
 * <p>
 * 회원가입:
 * passwordEncoder.encode(비밀번호)
 * </p>
 *
 * <p>
 * 로그인:
 * passwordEncoder.matches(입력값, 저장된 해시)
 * </p>
 *
 * BCrypt는 복호화가 불가능한 단방향 해시 알고리즘이다.
 */
@Configuration
public class PasswordConfig {

    /**
     * PasswordEncoder를 Spring Bean으로 등록한다.
     *
     * <p> MemberService와 LoginService에서 주입받아 사용한다. </p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}