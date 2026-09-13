package com.metionlabs.stocksimulator.controller.dto.dailyPrice;

import lombok.Getter;

// 주식 종목 가격 생성 후 생성된 주식 종목 가격 정보 반환
@Getter
public class DailyPricePostResult {

    private final Long dailyPriceId;

    public DailyPricePostResult(Long dailyPriceId) {
        this.dailyPriceId = dailyPriceId;
    }
}