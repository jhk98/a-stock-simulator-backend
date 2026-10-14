package com.myproject.stocksimulator.login.service;

import com.myproject.stocksimulator.domain.Member;
import com.myproject.stocksimulator.login.dto.LoginRequest;
import com.myproject.stocksimulator.login.dto.LoginResponse;
import com.myproject.stocksimulator.login.dto.TokenResponse;
import com.myproject.stocksimulator.repository.MemberRepository;
import com.myproject.stocksimulator.security.jwt.JwtTokenProvider;

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
 * 로그인 서비스
 *
 * 주요 기능:
 * 1. 아이디와 비밀번호 검증
 * 2. Access Token 발급
 * 3. Refresh Token 발급 및 Redis 저장
 * 4. Access Token 재발급
 * 5. 로그아웃
 *
 * 비밀번호는 BCrypt matches()로 검증한다.
 *
 * Access Token: JWT
 * Refresh Token: SecureRandom으로 생성한 랜덤 문자열
 */
@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    // 암호학적으로 안전한 난수 생성기
    private final SecureRandom secureRandom = new SecureRandom();

    // Refresh Token 유효기간 (기본 7일)
    @Value("${jwt.refresh-expiration-seconds:604800}")
    private long refreshExpirationSeconds;

    /**
     * 로그인 처리
     *
     * 처리 순서:
     * 1. 아이디로 회원 조회
     * 2. BCrypt 비밀번호 검증
     * 3. 마지막 로그인 시간 갱신
     * 4. Access Token 발급
     * 5. Refresh Token 발급 및 Redis 저장
     */
    @Transactional
    public LoginResult login(LoginRequest request) {

        // 1. 회원 조회
        Member member = memberRepository
                .findByLoginId(request.loginId())
                .orElseThrow(() -> new BadCredentialsException(
                        "아이디 또는 비밀번호가 올바르지 않습니다."
                ));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(
                request.password(),
                member.getPassword()
        )) {
            throw new BadCredentialsException(
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 3. 마지막 로그인 시간 변경
        member.updateLastAccessTime();

        // 4. Access Token 생성
        String accessToken =
                jwtTokenProvider.createAccessToken(member.getId());

        // 5. Refresh Token 생성 및 Redis 저장
        String refreshToken = issueRefreshToken(member.getId());

        // 6. 응답 데이터 구성
        LoginResponse response = new LoginResponse(
                accessToken,
                "Bearer",
                jwtTokenProvider.getAccessExpirationSeconds()
        );

        return new LoginResult(response, refreshToken);
    }

    /**
     * Refresh Token 발급
     *
     * 32바이트 랜덤 데이터를 Base64 URL 문자열로 변환한다.
     * 생성한 토큰은 Redis에 유효기간과 함께 저장한다.
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

    /**
     * Access Token 재발급
     *
     * 기존 Refresh Token을 소비하고
     * 새로운 Access Token과 Refresh Token을 발급한다.
     *
     * 기존 Refresh Token은 다시 사용할 수 없다.
     */
    public RefreshResult refresh(String oldRefreshToken) {

        // 1. 기존 Refresh Token 조회 및 삭제
        Long memberId =
                refreshTokenStore.consume(oldRefreshToken);

        if (memberId == null) {
            throw new BadCredentialsException(
                    "Refresh Token이 유효하지 않습니다."
            );
        }

        // 2. 회원 존재 여부 확인
        if (!memberRepository.existsById(memberId)) {
            throw new BadCredentialsException(
                    "존재하지 않는 회원입니다."
            );
        }

        // 3. 새 토큰 발급
        String accessToken =
                jwtTokenProvider.createAccessToken(memberId);

        String refreshToken =
                issueRefreshToken(memberId);

        // 4. 응답 생성
        TokenResponse response = new TokenResponse(
                accessToken,
                "Bearer",
                jwtTokenProvider.getAccessExpirationSeconds()
        );

        return new RefreshResult(response, refreshToken);
    }

    /**
     * 로그아웃 처리
     *
     * 전달받은 Refresh Token을 Redis에서 삭제한다.
     *
     * 이미 발급된 Access Token은 만료 전까지
     * 유효할 수 있으므로 프론트에서도 반드시 제거해야 한다.
     */
    public void logout(String refreshToken) {

        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenStore.delete(refreshToken);
        }
    }

    // 로그인 결과: JSON 응답 + Refresh Token 쿠키용 값
    public record LoginResult(
            LoginResponse response,
            String refreshToken
    ) {
    }

    // 재발급 결과: JSON 응답 + 새 Refresh Token
    public record RefreshResult(
            TokenResponse response,
            String refreshToken
    ) {
    }
}