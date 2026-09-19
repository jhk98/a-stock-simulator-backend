package com.myproject.stocksimulator.controller.dto.stock;

import lombok.Getter;

// 주식 종목 생성 후 생성된 주식 종목 정보 반환
@Getter
public class StockPostResult {

    private final Long stockId;

    public StockPostResult(Long stockId) {
        this.stockId = stockId;
    }
}