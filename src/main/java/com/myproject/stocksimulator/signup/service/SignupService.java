package com.myproject.stocksimulator.signup.service;

import com.myproject.stocksimulator.email.service.EmailService;
import com.myproject.stocksimulator.member.entity.Member;
import com.myproject.stocksimulator.member.repository.MemberRepository;
import com.myproject.stocksimulator.signup.dto.SignupRequest;
import com.myproject.stocksimulator.signup.dto.SignupResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Locale;

/**
 * 회원가입을 담당한다.
 *
 * <p> 아이디·이메일 중복과 이메일 인증 여부를 확인하고, 비밀번호를 해시 처리한 뒤 회원을 저장한다. </p>
 */
@Service
@RequiredArgsConstructor
public class SignupService {

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    /**
     * true면 이미 사용 중인 아이디다.
     */
    @Transactional(readOnly = true)
    public boolean checkLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    /**
     * true면 이미 사용 중인 이메일이다.
     */
    @Transactional(readOnly = true)
    public boolean checkEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return memberRepository.existsByEmail(normalizedEmail);
    }

    /**
     * 검증된 가입 요청으로 회원을 생성한다.
     * 요청 형식 검증은 Controller의 @Valid와 SignupRequest가 담당한다.
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());

        // 화면에서 중복 확인을 했더라도 가입 시점에 다시 검사한다.
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 아이디입니다."
            );
        }

        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 이메일입니다."
            );
        }

        // 클라이언트가 보낸 인증 여부 대신 Redis의 인증 상태를 확인한다.
        if (!emailService.isVerified(email)) {
            throw new IllegalStateException(
                    "이메일 인증을 완료해주세요."
            );
        }

        // 평문 비밀번호는 저장하지 않는다.
        String encodedPassword =
                passwordEncoder.encode(request.password());

        Member member = Member.builder()
                .loginId(request.loginId())
                .password(encodedPassword)
                .name(request.name())
                .phoneNumber(formatPhoneNumber(request.phoneNumber()))
                .email(email)
                .build();

        try {
            // INSERT를 실행해 DB의 UNIQUE 제약조건도 확인한다.
            memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 아이디 또는 이메일입니다.",
                    e
            );
        }

        // DB 저장이 확정된 뒤 이메일 인증 상태를 정리한다.
        clearVerificationAfterCommit(email);

        return new SignupResponse(member.getId());
    }

    /**
     * 이메일 중복 검사와 저장에 같은 표기 규칙을 적용한다.
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 현재 회원 테이블과 호환되도록 전화번호 저장 형식을 유지한다.
     * 예: 01012345678 → 010-1234-5678
     */
    private String formatPhoneNumber(String phoneNumber) {
        return phoneNumber.substring(0, 3)
                + "-"
                + phoneNumber.substring(3, 7)
                + "-"
                + phoneNumber.substring(7);
    }

    /**
     * DB 트랜잭션이 롤백되면 인증 완료 상태를 지우지 않는다.
     */
    private void clearVerificationAfterCommit(String email) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        emailService.clearVerification(email);
                    }
                }
        );
    }
}