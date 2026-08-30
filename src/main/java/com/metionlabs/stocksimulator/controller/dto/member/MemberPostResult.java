package com.metionlabs.stocksimulator.controller.dto.member;

import lombok.Getter;

@Getter
public class MemberPostResult {

    private final Long memberId;

    public MemberPostResult(Long memberId) {
        this.memberId = memberId;
    }
}