package com.metionlabs.stocksimulator.controller.dto.currentPrice;

import lombok.Getter;

// 주식 현재가 생성 응답
@Getter
public class CurrentPricePostResult {

    private final Long currentPriceId;

    public CurrentPricePostResult(Long currentPriceId) {
        this.currentPriceId = currentPriceId;
    }
}