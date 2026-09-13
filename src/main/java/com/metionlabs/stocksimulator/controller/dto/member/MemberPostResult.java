package com.metionlabs.stocksimulator.controller.dto.member;

import lombok.Getter;

// 회원가입 후 생성된 회원의 정보 반환
@Getter
public class MemberPostResult {

    private final Long memberId;

    public MemberPostResult(Long memberId) {
        this.memberId = memberId;
    }
}