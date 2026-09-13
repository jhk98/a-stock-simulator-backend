package com.metionlabs.stocksimulator.controller.dto.holding;

import lombok.Getter;

// 회원 보유 주식 생성 후 생성된 회원 보유 주식 정보 반환
@Getter
public class HoldingPostResult {

    private final Long holdingId;

    public HoldingPostResult(Long holdingId) {
        this.holdingId = holdingId;
    }
}