package com.myproject.stocksimulator.auth.service;

import com.myproject.stocksimulator.auth.dto.LoginRequest;
import com.myproject.stocksimulator.auth.dto.LoginResponse;
import com.myproject.stocksimulator.auth.dto.TokenResponse;
import com.myproject.stocksimulator.domain.member.Member;
import com.myproject.stocksimulator.domain.member.MemberStatus;
import com.myproject.stocksimulator.repository.MemberRepository;
import com.myproject.stocksimulator.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JWT 기반 인증 Service
 *
 * 로그인, Access Token 재발급, 로그아웃을 담당한다.
 *
 * Refresh Token은 Redis에 저장하고,
 * 재발급 시 기존 토큰을 새로운 토큰으로 교체한다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String LOGIN_FAILURE_MESSAGE =
            "아이디 또는 비밀번호가 올바르지 않습니다.";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    /**
     * 로그인 결과
     *
     * response     : 클라이언트에 반환할 로그인 정보
     * refreshToken : HttpOnly 쿠키에 저장할 토큰
     */
    public record LoginResult(
            LoginResponse response,
            String refreshToken
    ) {
    }

    /**
     * 토큰 재발급 결과
     */
    public record RefreshResult(
            TokenResponse response,
            String refreshToken
    ) {
    }

    /**
     * 로그인
     *
     * 1. 아이디로 회원 조회
     * 2. 비밀번호 검증
     * 3. 회원 활성 상태 확인
     * 4. 마지막 접속 시간 갱신
     * 5. Access / Refresh Token 발급
     * 6. Refresh Token Redis 저장
     */
    @Transactional
    public LoginResult login(LoginRequest request) {

        // 1. 회원 조회
        Member member = memberRepository
                .findByLoginId(request.loginId())
                .orElseThrow(() ->
                        new BadCredentialsException(
                                LOGIN_FAILURE_MESSAGE
                        )
                );

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(
                request.password(),
                member.getPasswd()
        )) {
            throw new BadCredentialsException(
                    LOGIN_FAILURE_MESSAGE
            );
        }

        // 3. 회원 상태 확인
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BadCredentialsException(
                    LOGIN_FAILURE_MESSAGE
            );
        }

        // 4. 마지막 접속 시간 갱신
        member.updateLastAccessTime();

        // 5. Access Token 발급
        String accessToken =
                jwtTokenProvider.generateAccessToken(
                        member.getId()
                );

        // 6. Refresh Token 발급
        String refreshToken =
                jwtTokenProvider.generateRefreshToken(
                        member.getId()
                );

        // 7. Refresh Token Redis 저장
        refreshTokenStore.save(
                member.getId(),
                refreshToken,
                JwtTokenProvider.REFRESH_EXPIRATION
        );

        // 8. 로그인 응답 생성
        LoginResponse response = new LoginResponse(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                accessToken,
                "Bearer",
                JwtTokenProvider.ACCESS_EXPIRATION.toSeconds()
        );

        return new LoginResult(
                response,
                refreshToken
        );
    }

    /**
     * Access Token 재발급
     *
     * 1. Refresh Token 검증
     * 2. 활성 회원 조회
     * 3. 새로운 Refresh Token 생성
     * 4. Redis에서 기존 토큰 교체
     * 5. 새로운 Access Token 발급
     */
    @Transactional(readOnly = true)
    public RefreshResult refresh(String refreshToken) {

        // 1. Refresh Token 검증 및 회원 ID 추출
        Long memberId = validateRefreshToken(refreshToken);

        // 2. 활성 회원 조회
        Member member = memberRepository
                .findByIdAndStatus(
                        memberId,
                        MemberStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "유효하지 않은 회원입니다."
                        )
                );

        // 3. 새로운 Refresh Token 발급
        String newRefreshToken =
                jwtTokenProvider.generateRefreshToken(
                        member.getId()
                );

        // 4. Redis에서 Refresh Token 교체
        boolean rotated = refreshTokenStore.rotate(
                member.getId(),
                refreshToken,
                newRefreshToken,
                JwtTokenProvider.REFRESH_EXPIRATION
        );

        // 기존 토큰이 만료되었거나 이미 사용된 경우
        if (!rotated) {
            throw new BadCredentialsException(
                    "만료되었거나 이미 사용된 Refresh Token입니다."
            );
        }

        // 5. 새로운 Access Token 발급
        String accessToken =
                jwtTokenProvider.generateAccessToken(
                        member.getId()
                );

        // 6. 재발급 응답 생성
        TokenResponse response = new TokenResponse(
                accessToken,
                "Bearer",
                JwtTokenProvider.ACCESS_EXPIRATION.toSeconds()
        );

        return new RefreshResult(
                response,
                newRefreshToken
        );
    }

    /**
     * 로그아웃
     *
     * Refresh Token이 유효하면
     * Redis에서 현재 저장된 토큰과 비교하여 폐기한다.
     *
     * 쿠키 삭제는 Controller에서 처리한다.
     */
    public void logout(String refreshToken) {

        // 유효하지 않은 토큰은 별도 오류 없이 종료
        if (refreshToken == null
                || !jwtTokenProvider.validateRefreshToken(refreshToken)) {
            return;
        }

        Long memberId =
                jwtTokenProvider.getMemberId(refreshToken);

        // Redis에서 Refresh Token 폐기
        refreshTokenStore.revoke(
                memberId,
                refreshToken
        );
    }

    /**
     * Refresh Token 검증
     *
     * 서명, 만료시간, 토큰 종류를 확인하고
     * 정상적인 경우 회원 ID를 반환한다.
     */
    private Long validateRefreshToken(String refreshToken) {

        if (refreshToken == null
                || !jwtTokenProvider.validateRefreshToken(refreshToken)) {

            throw new BadCredentialsException(
                    "Refresh Token이 유효하지 않습니다."
            );
        }

        return jwtTokenProvider.getMemberId(refreshToken);
    }
}