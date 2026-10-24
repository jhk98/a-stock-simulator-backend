package com.myproject.stocksimulator.member.service;

import com.myproject.stocksimulator.email.service.EmailService;
import com.myproject.stocksimulator.member.dto.MemberResponse;
import com.myproject.stocksimulator.member.dto.MemberUpdateRequest;
import com.myproject.stocksimulator.member.entity.Member;
import com.myproject.stocksimulator.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * 로그인한 회원의 개인정보 조회와 수정
 *
 * 수정 가능한 항목은 비밀번호, 전화번호, 이메일이다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * 현재 로그인 회원의 정보를 조회한다.
     * 비밀번호는 MemberResponse에 포함되지 않는다.
     */
    public MemberResponse getById(Long memberId) {
        requireMemberId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("회원을 찾을 수 없습니다.")
                );

        return MemberResponse.from(member);
    }

    /**
     * 전달된 항목만 변경한다.
     * 여러 항목을 변경하더라도 MySQL에는 하나의 트랜잭션으로 반영한다.
     */
    @Transactional
    public MemberResponse update(
            Long memberId,
            MemberUpdateRequest request
    ) {
        requireMemberId(memberId);

        if (request.newPassword() == null
                && request.phoneNumber() == null
                && request.email() == null) {
            throw new IllegalArgumentException("수정할 항목을 입력해주세요.");
        }

        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("회원을 찾을 수 없습니다.")
                );

        // JWT 인증과 별도로 현재 비밀번호를 다시 확인한다.
        if (!passwordEncoder.matches(
                request.currentPassword(),
                member.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "현재 비밀번호가 일치하지 않습니다."
            );
        }

        String encodedPassword = preparePassword(member, request.newPassword());
        String phoneNumber = formatPhoneNumber(request.phoneNumber());
        String email = normalizeEmail(request.email());

        boolean emailChanged = email != null
                && !email.equalsIgnoreCase(member.getEmail());

        // 인증번호를 소비하기 전에 다른 입력과 이메일 중복을 확인한다.
        if (emailChanged) {
            if (memberRepository.existsByEmail(email)) {
                throw new IllegalArgumentException(
                        "이미 사용 중인 이메일입니다."
                );
            }

            if (request.emailCode() == null) {
                throw new IllegalArgumentException(
                        "새 이메일로 받은 인증번호를 입력해주세요."
                );
            }
        } else if (request.emailCode() != null) {
            throw new IllegalArgumentException(
                    "인증번호는 이메일을 변경할 때만 입력해주세요."
            );
        }

        boolean passwordChanged = encodedPassword != null;
        boolean phoneChanged = phoneNumber != null
                && !phoneNumber.equals(member.getPhoneNumber());

        if (!passwordChanged && !phoneChanged && !emailChanged) {
            throw new IllegalArgumentException(
                    "기존 정보와 다른 내용을 입력해주세요."
            );
        }

        if (emailChanged) {
            // 저장 요청에 포함된 인증번호를 직접 검증한다.
            boolean verified = emailService.verifyCode(
                    email,
                    request.emailCode()
            );

            if (!verified) {
                throw new IllegalArgumentException(
                        "이메일 인증번호가 올바르지 않거나 만료되었습니다."
                );
            }

            // 기존 서비스가 생성한 회원가입용 인증 완료 상태는 남기지 않는다.
            emailService.clearVerification(email);
        }

        if (passwordChanged) {
            member.changePassword(encodedPassword);
        }

        if (phoneChanged) {
            member.changePhoneNumber(phoneNumber);
        }

        if (emailChanged) {
            member.changeEmail(email);
        }

        /*
         * 관리 중인 엔티티이므로 변경 감지로 UPDATE된다.
         * flush로 UNIQUE 위반을 확인하고 수정 시각을 응답에 반영한다.
         */
        memberRepository.flush();

        return MemberResponse.from(member);
    }

    /**
     * 비밀번호 변경이 요청된 경우에만 해시를 생성한다.
     */
    private String preparePassword(Member member, String newPassword) {
        if (newPassword == null) {
            return null;
        }

        // BCrypt 입력 크기를 초과하지 않도록 바이트 길이도 확인한다.
        if (newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "새 비밀번호의 바이트 길이가 너무 깁니다."
            );
        }

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new IllegalArgumentException(
                    "새 비밀번호는 현재 비밀번호와 다르게 입력해주세요."
            );
        }

        return passwordEncoder.encode(newPassword);
    }

    /**
     * 가입 기능과 같은 전화번호 저장 형식을 사용한다.
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        return phoneNumber.substring(0, 3)
                + "-"
                + phoneNumber.substring(3, 7)
                + "-"
                + phoneNumber.substring(7);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        String normalized = email.trim().toLowerCase(Locale.ROOT);

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        return normalized;
    }

    private void requireMemberId(Long memberId) {
        if (memberId == null) {
            throw new BadCredentialsException("로그인이 필요합니다.");
        }
    }
}