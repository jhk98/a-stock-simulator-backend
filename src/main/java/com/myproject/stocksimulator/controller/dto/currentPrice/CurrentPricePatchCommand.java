package com.myproject.stocksimulator.controller.dto.currentPrice;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 주식 현재가 수정할 수 있는 정보
@Getter
@NoArgsConstructor
public class CurrentPricePatchCommand {

    private Long currentPrice;
    private Long changePrice;
    private BigDecimal changeRate;
    private Long currentVolume;
}