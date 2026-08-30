package com.metionlabs.stocksimulator.controller.dto.dailyPrice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DailyPricePostCommand {

    @NotNull(message = "stockId 는 필수입니다.")
    private Long stockId;

    @NotBlank(message = "시가는 필수입니다.")
    private Long openPrice;

    @NotBlank(message = "종가는 필수입니다.")
    private Long closePrice;

    @NotBlank(message = "고가는 필수입니다.")
    private Long highPrice;

    @NotBlank(message = "저가는 필수입니다.")
    private Long lowPrice;

    @NotBlank(message = "거래량은 필수입니다.")
    private Long volume;
}