package com.myproject.stocksimulator.controller.dto.login;

import com.myproject.stocksimulator.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginPostResult {

    // 회원 ID
    private Long memberId;

    // 로그인 아이디
    private String loginId;

    // 회원 이름
    private String name;

    public static LoginPostResult from (Member member) {
        return new LoginPostResult(
                member.getId(),
                member.getLoginId(),
                member.getName()
        );
    }
}