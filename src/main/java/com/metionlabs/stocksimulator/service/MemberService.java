package com.metionlabs.stocksimulator.service;

import com.metionlabs.stocksimulator.controller.dto.member.MemberPatchCommand;
import com.metionlabs.stocksimulator.controller.dto.member.MemberPostCommand;
import com.metionlabs.stocksimulator.domain.member.Member;
import com.metionlabs.stocksimulator.domain.member.MemberRole;
import com.metionlabs.stocksimulator.domain.member.MemberStatus;
import com.metionlabs.stocksimulator.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    // 생성자 주입
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // ========================================================================
    // C — 생성
    // ========================================================================

    // 회원 생성
    public Member create(MemberPostCommand command) {
        // 로그인 아이디 중복 검사
        if(memberRepository.existsByLoginId(command.getLoginId())) {
            throw new IllegalStateException(
                    "이미 사용 중인 로그인 아이디입니다. " + command.getLoginId()
            );
        }

        // 이메일 중복 검사
        if(memberRepository.existsByEmail(command.getEmail())) {
            throw new IllegalStateException(
                    "이미 사용 중인 이메일입니다. " + command.getEmail()
            );
        }

        // 전화번호 중복 검사
        if(memberRepository.existsByPhoneNumber(command.getPhoneNumber())) {
            throw new IllegalStateException(
                    "이미 사용 중인 전화번호입니다. " + command.getPhoneNumber()
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Member member = Member.builder()
                .loginId(command.getLoginId())
                .name(command.getName())
                .email(command.getEmail())
                .phoneNumber(command.getPhoneNumber())
                .rgstNumber(command.getRgstNumber())
                .passwd(command.getPasswd())
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .createdAt(now)
                .modifiedAt(now)
                .build();

        // 저장 (id가 null인 엔티티라 save()는 INSERT로 동작)
        return memberRepository.save(member);
    }

    // ========================================================================
    // R — 조회
    // ========================================================================

    // id로 조회 없으면 예외
    @Transactional(readOnly = true)
    public Member getById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다. id = " + id));
    }

    // ========================================================================
    // U — 수정
    // ========================================================================

    // 회원 정보 수정
    public Member update(Long id, MemberPatchCommand command) {
        Member member = getById(id);

        // 이메일을 바꾸려는 경우에만 중복 검사
        String newEmail = command.getEmail();
        if(newEmail != null && !newEmail.equals(member.getEmail())
                && memberRepository.existsByEmail(newEmail)) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다. " + newEmail);
        }

        // 전화번호를 바꾸려는 경우에만 중복 검사
        String newPhoneNumber = command.getPhoneNumber();
        if(newPhoneNumber != null && !newPhoneNumber.equals(member.getPhoneNumber())
                && memberRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new IllegalStateException("이미 사용 중인 전화번호입니다. " + newPhoneNumber);
        }

        member.update(
                command.getName(),
                command.getEmail(),
                command.getPhoneNumber(),
                command.getPasswd()
        );

        return member;
    }

    // ========================================================================
    // D — 삭제
    // ========================================================================

    // 회원 정보 삭제
    public void delete(Long id) {
        Member member = getById(id);
        memberRepository.delete(member);
    }
}