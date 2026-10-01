package com.myproject.stocksimulator.service;

import com.myproject.stocksimulator.controller.dto.login.LoginPostCommand;
import com.myproject.stocksimulator.domain.member.Member;
import com.myproject.stocksimulator.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    private final MemberRepository memberRepository;

    // 생성자 주입
    public LoginService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 로그인
    @Transactional(readOnly = true)
    public Member login(LoginPostCommand command) {

        // 1. 로그인 아이디로 회원 조회
        Member member = memberRepository.findByLoginId(command.getLoginId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "아이디 또는 비밀번호가 올바르지 않습니다."
                        )
                );

        // 2. 비밀번호 확인
        if (!member.getPasswd().equals(command.getPassword())) {
            throw new IllegalArgumentException(
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 3. 로그인 성공
        return member;
    }
}