package com.myproject.stocksimulator.signup.service;

import com.myproject.stocksimulator.domain.Member;
import com.myproject.stocksimulator.email.service.EmailService;
import com.myproject.stocksimulator.repository.MemberRepository;
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
 * 회원가입 서비스
 *
 * <p>
 * 주요 역할
 * 1. 로그인 아이디 중복 확인
 * 2. 이메일 중복 확인
 * 3. 이메일 인증 완료 여부 확인
 * 4. 비밀번호 BCrypt 암호화
 * 5. 전화번호 형식 변환
 * 6. 회원정보 저장
 * 7. 회원가입 성공 후 이메일 인증 상태 제거
 * </p>
 *
 * 회원가입 관련 비즈니스 로직은 이 클래스에서 처리한다.
 */
@Service
@RequiredArgsConstructor
public class SignupService {

    // 회원 데이터 조회 및 저장
    private final MemberRepository memberRepository;

    // Redis 이메일 인증 상태 확인 및 제거
    private final EmailService emailService;

    // BCrypt 비밀번호 암호화
    private final PasswordEncoder passwordEncoder;


    // ============================================================
    // 1. 로그인 아이디 중복 확인
    // ============================================================

    /**
     * 로그인 아이디 중복 확인
     *
     * <p> 회원가입 화면에서 사용자가 입력한 아이디가 이미 등록되어 있는지 확인한다.
     *
     * @param loginId   확인할 로그인 아이디
     * @return          true: 중복 / false: 사용 가능
     */
    @Transactional(readOnly = true)
    public boolean checkLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }


    // ============================================================
    // 2. 이메일 중복 확인
    // ============================================================

    /**
     * 이메일 중복 확인
     *
     * <p> 이메일은 대소문자 차이로 중복 가입하는 것을 방지하기 위해 소문자로 정규화한 뒤 조회한다. </p>
     *
     * @param email     확인할 이메일
     * @return          true: 중복 / false: 사용 가능
     */
    @Transactional(readOnly = true)
    public boolean checkEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        return memberRepository.existsByEmail(normalizedEmail);
    }


    // ============================================================
    // 3. 회원가입
    // ============================================================

    /**
     * 회원가입
     *
     * <p>
     * 처리 순서
     * 1. 로그인 아이디 중복 확인
     * 2. 이메일 중복 확인
     * 3. 이메일 인증 완료 여부 확인
     * 4. 비밀번호 BCrypt 암호화
     * 5. 전화번호 하이픈 추가
     * 6. Member Entity 생성 및 저장
     * 7. DB 커밋 성공 후 Redis 인증 상태 제거
     * </p>
     *
     * @param request   회원가입 요청 정보
     * @return          생성된 회원 ID
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        // ============================================================
        // 1. 로그인 아이디 중복 확인
        // ============================================================

        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }


        // ============================================================
        // 2. 이메일 중복 확인
        // ============================================================

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }


        // ============================================================
        // 3. 이메일 인증 완료 여부 확인
        // ============================================================

        if (!emailService.isVerified(email)) {
            throw new IllegalStateException("이메일 인증을 완료해주세요.");
        }


        // ============================================================
        // 4. 비밀번호 BCrypt 암호화
        // ============================================================

        // 평문 비밀번호를 DB에 저장하지 않는다.
        String encodedPassword = passwordEncoder.encode(request.password());


        // ============================================================
        // 5. 전화번호 형식 변환
        // ============================================================

        // 01012345678 → 010-1234-5678
        String phoneNumber = request.phoneNumber();

        String formattedPhoneNumber =
                phoneNumber.substring(0, 3) + "-" +
                        phoneNumber.substring(3, 7) + "-" +
                        phoneNumber.substring(7);


        // ============================================================
        // 6. Member Entity 생성
        // ============================================================

        Member member = Member.builder()
                .loginId(request.loginId())
                .password(encodedPassword)
                .name(request.name())
                .phoneNumber(formattedPhoneNumber)
                .email(email)
                .build();


        // ============================================================
        // 7. MySQL 회원정보 저장
        // ============================================================

        // INSERT를 실행하여 DB UNIQUE 제약조건 위반 여부를 확인한다.
        try {
            memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 아이디 또는 이메일입니다.", e
            );
        }


        // ============================================================
        // 8. 트랜잭션 커밋 성공 후 이메일 인증 상태 제거
        // ============================================================

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        emailService.clearVerification(email);
                    }
                }
        );


        // ============================================================
        // 9. 회원가입 결과 반환
        // ============================================================

        return new SignupResponse(member.getId());
    }
}