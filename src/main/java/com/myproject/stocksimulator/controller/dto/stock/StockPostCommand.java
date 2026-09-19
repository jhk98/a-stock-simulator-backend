package com.myproject.stocksimulator.controller.dto.stock;

import com.myproject.stocksimulator.domain.stock.StockType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 주식 종목 등록 시 필요한 정보
@Getter
@NoArgsConstructor
public class StockPostCommand {

    @NotBlank(message = "주식코드는 필수입니다.")
    private String stockCode;

    @NotBlank(message = "주식이름은 필수입니다.")
    private String stockName;

    @NotNull(message = "주식타입은 필수입니다.")
    private StockType stockType;
}