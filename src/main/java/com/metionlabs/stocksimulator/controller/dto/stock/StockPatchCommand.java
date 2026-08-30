package com.metionlabs.stocksimulator.controller.dto.stock;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 주식 종목 부분 수정 요청 (PATCH)
@Getter
@NoArgsConstructor
public class StockPatchCommand {

    private String stockCode;

    private String stockName;

    private String stockType;
}