package com.metionlabs.stocksimulator.controller.dto.stock;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockPostCommand {

    @NotBlank(message = "주식코드는 필수입니다.")
    private String stockCode;

    @NotBlank(message = "주식이름은 필수입니다.")
    private String stockName;

    @NotBlank(message = "주식타입은 필수입니다.")
    private String stockType;
}