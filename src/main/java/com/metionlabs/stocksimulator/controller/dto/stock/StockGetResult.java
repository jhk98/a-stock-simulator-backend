package com.metionlabs.stocksimulator.controller.dto.stock;

import com.metionlabs.stocksimulator.domain.Stock;
import lombok.Getter;

// 주식 종목 조회 응답
@Getter
public class StockGetResult {

    private final Long id;
    private final String stockCode;
    private final String stockName;
    private final String stockType;

    private StockGetResult(Stock stock) {
        this.id = stock.getId();
        this.stockCode = stock.getStockCode();
        this.stockName = stock.getStockName();
        this.stockType = stock.getStockType();
    }

    public static StockGetResult from(Stock stock) {
        return new StockGetResult(stock);
    }
}