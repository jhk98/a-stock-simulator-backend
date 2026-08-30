package com.metionlabs.stocksimulator.controller.dto.currentPrice;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 주식 현재가 부분 수정 요청
@Getter
@NoArgsConstructor
public class CurrentPricePatchCommand {

    private Long currentPrice;
    private Long changePrice;
    private double changeRate;
    private Long currentVolume;
}