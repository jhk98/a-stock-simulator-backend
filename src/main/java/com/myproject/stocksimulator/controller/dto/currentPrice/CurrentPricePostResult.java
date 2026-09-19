package com.myproject.stocksimulator.controller.dto.currentPrice;

import lombok.Getter;

// 주식 현재가 생성 후 생성된 주식 현재가 정보 반환
@Getter
public class CurrentPricePostResult {

    private final Long currentPriceId;

    public CurrentPricePostResult(Long currentPriceId) {
        this.currentPriceId = currentPriceId;
    }
}