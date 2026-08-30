package com.metionlabs.stocksimulator.controller.dto.dailyPrice;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 주식 종목 가격 부분 수정 요청
@Getter
@NoArgsConstructor
public class DailyPricePatchCommand {

    private Long openPrice;
    private Long closePrice;
    private Long highPrice;
    private Long lowPrice;
    private Long volume;
}