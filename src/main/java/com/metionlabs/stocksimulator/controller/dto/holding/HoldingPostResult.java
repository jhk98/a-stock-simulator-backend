package com.metionlabs.stocksimulator.controller.dto.holding;

import lombok.Getter;

// 회원 보유 주식 응답
@Getter
public class HoldingPostResult {

    private final Long holdingId;

    public HoldingPostResult(Long holdingId) {
        this.holdingId = holdingId;
    }
}