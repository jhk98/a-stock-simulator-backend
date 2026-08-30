package com.metionlabs.stocksimulator.controller.dto.stock;

import lombok.Getter;

// 주식 종목 만들기 응답
@Getter
public class StockPostResult {

    private final Long stockId;

    public StockPostResult(Long stockId) {
        this.stockId = stockId;
    }
}