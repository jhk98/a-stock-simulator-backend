package com.myproject.stocksimulator.auth.service;

import com.myproject.stocksimulator.member.entity.Member;
import com.myproject.stocksimulator.auth.dto.LoginRequest;
import com.myproject.stocksimulator.auth.dto.LoginResponse;
import com.myproject.stocksimulator.auth.dto.TokenResponse;
import com.myproject.stocksimulator.member.repository.MemberRepository;
import com.myproject.stocksimulator.security.jwt.JwtTokenProvider;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

/**
 * 로그인 및 토큰 관리 서비스
 *
 * <p>
 * Access Token:
 * - JWT로 발급한다.
 * - JSON 응답으로 프론트엔드에 전달한다.
 * </p>
 *
 * Refresh Token:
 * - 안전한 난수로 생성한다.
 * - Redis에 유효기간과 함께 저장한다.
 * - Controller에서 HttpOnly 쿠키로 전달한다.
 *
 * <p> 로그인과 토큰 재발급 응답에는 사용자 정보도 포함한다. </p>
 */
@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    private final SecureRandom secureRandom = new SecureRandom();

    // Refresh Token 유효기간: 기본 7일
    @Value("${jwt.refresh-expiration-seconds:604800}")
    private long refreshExpirationSeconds;

    /**
     * 잘못된 토큰 유효기간 설정을 애플리케이션 시작 시 확인한다.
     */
    @PostConstruct
    public void validateConfiguration() {
        if (refreshExpirationSeconds <= 0) {
            throw new IllegalStateException(
                    "Refresh Token 유효기간은 0보다 커야 합니다."
            );
        }
    }

    // ============================================================
    // 1. 로그인
    // ============================================================

    /**
     * 아이디와 비밀번호를 검증하고 토큰 및 사용자 정보를 반환한다.
     */
    @Transactional
    public LoginResult login(LoginRequest request) {

        // Controller의 @Valid 외에 서비스에서도 기본 입력값을 확인한다.
        if (request == null
                || request.loginId() == null
                || request.loginId().isBlank()
                || request.password() == null
                || request.password().isBlank()) {
            throw new BadCredentialsException(
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 아이디 앞뒤 공백만 제거한다.
        // 비밀번호는 입력값 그대로 검증한다.
        String loginId = request.loginId().trim();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BadCredentialsException(
                        "아이디 또는 비밀번호가 올바르지 않습니다."
                ));

        if (!passwordEncoder.matches(
                request.password(),
                member.getPassword()
        )) {
            throw new BadCredentialsException(
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // JPA 변경 감지를 통해 마지막 로그인 시간을 저장한다.
        member.updateLastAccessTime();

        String accessToken =
                jwtTokenProvider.createAccessToken(member.getId());

        String refreshToken = issueRefreshToken(member.getId());

        LoginResponse response = new LoginResponse(
                accessToken,
                "Bearer",
                jwtTokenProvider.getAccessExpirationSeconds(),
                member.getId(),
                member.getLoginId(),
                member.getName()
        );

        return new LoginResult(response, refreshToken);
    }

    // ============================================================
    // 2. 토큰 재발급
    // ============================================================

    /**
     * 기존 Refresh Token을 소비하고 새로운 토큰을 발급한다.
     *
     * <p>
     * 새로고침 후에도 프론트엔드가 사용자 정보를 복원할 수 있도록
     * 회원 ID, 로그인 아이디, 이름을 함께 반환한다.
     * </p>
     *
     * 기존 Refresh Token은 한 번만 사용할 수 있다.
     */
    public RefreshResult refresh(String oldRefreshToken) {

        if (oldRefreshToken == null || oldRefreshToken.isBlank()) {
            throw new BadCredentialsException(
                    "Refresh Token이 없습니다."
            );
        }

        // Redis에서 기존 토큰 조회와 삭제를 한 번에 수행한다.
        Long memberId = refreshTokenStore.consume(oldRefreshToken);

        if (memberId == null) {
            throw new BadCredentialsException(
                    "Refresh Token이 만료되었거나 유효하지 않습니다."
            );
        }

        // 존재 여부만 확인하지 않고 응답에 필요한 회원 정보를 조회한다.
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BadCredentialsException(
                        "존재하지 않는 회원입니다."
                ));

        String accessToken =
                jwtTokenProvider.createAccessToken(member.getId());

        String refreshToken = issueRefreshToken(member.getId());

        TokenResponse response = new TokenResponse(
                accessToken,
                "Bearer",
                jwtTokenProvider.getAccessExpirationSeconds(),
                member.getId(),
                member.getLoginId(),
                member.getName()
        );

        return new RefreshResult(response, refreshToken);
    }

    // ============================================================
    // 3. 로그아웃
    // ============================================================

    /**
     * Redis에서 Refresh Token을 삭제한다.
     *
     * <p>
     * 브라우저 쿠키 삭제는 Controller에서 처리한다.
     * 프론트엔드도 메모리에 저장된 인증 정보를 제거해야 한다.
     * </p>
     *
     * 이미 발급된 Access Token은 만료 전까지 유효할 수 있다.
     */
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        refreshTokenStore.delete(refreshToken);
    }

    // ============================================================
    // 4. Refresh Token 생성 및 저장
    // ============================================================

    /**
     * 32바이트 난수를 URL-safe Base64 문자열로 변환한다.
     * 생성한 토큰을 Redis에 유효기간과 함께 저장한다.
     */
    private String issueRefreshToken(Long memberId) {

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        refreshTokenStore.save(
                token,
                memberId,
                Duration.ofSeconds(refreshExpirationSeconds)
        );

        return token;
    }

    // ============================================================
    // 5. Controller에 전달할 결과
    // ============================================================

    /**
     * response는 JSON으로,
     * refreshToken은 HttpOnly 쿠키로 전달한다.
     */
    public record LoginResult(
            LoginResponse response,
            String refreshToken
    ) {
    }

    /**
     * 재발급 결과 역시 JSON 응답과 쿠키용 토큰을 분리한다.
     */
    public record RefreshResult(
            TokenResponse response,
            String refreshToken
    ) {
    }
}