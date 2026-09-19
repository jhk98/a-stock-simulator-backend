package com.myproject.stocksimulator.controller.dto.stock;

import com.myproject.stocksimulator.domain.stock.Stock;
import com.myproject.stocksimulator.domain.stock.StockType;
import lombok.Getter;

// 주식 종목 조회 시 반환할 정보
@Getter
public class StockGetResult {

    private final Long id;
    private final String stockCode;
    private final String stockName;
    private final StockType stockType;

    // 엔티티를 전달받아서 DTO객체로 변환하는 생성자
    private StockGetResult(Stock stock) {
        this.id = stock.getId();
        this.stockCode = stock.getStockCode();
        this.stockName = stock.getStockName();
        this.stockType = stock.getStockType();
    }

    // 엔티티로부터 GetResult DTO를 생성하는 메서드
    public static StockGetResult from(Stock stock) {
        return new StockGetResult(stock);
    }
}