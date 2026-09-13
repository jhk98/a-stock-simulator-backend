package com.metionlabs.stocksimulator.controller.dto.stock;

import com.metionlabs.stocksimulator.domain.stock.StockType;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 주식 종목 수정할 수 있는 정보
@Getter
@NoArgsConstructor
public class StockPatchCommand {

    private String stockCode;

    private String stockName;

    private StockType stockType;
}