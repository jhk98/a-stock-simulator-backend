package com.myproject.stocksimulator.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 해시 처리 설정 클래스
 *
 * <p>회원가입 및 비밀번호 변경 시 사용할 PasswordEncoder를
 * Spring Bean으로 등록한다.</p>
 *
 * <p>BCrypt 알고리즘을 사용하여 비밀번호를 단방향 해시 처리한다.
 * BCrypt로 생성된 해시는 복호화할 수 없으며,
 * 로그인 시 입력한 비밀번호와 저장된 해시를 비교하여 인증한다.</p>
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 비밀번호 해시 및 검증에 사용할 PasswordEncoder 등록
     *
     * @return BCrypt 알고리즘을 사용하는 PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}