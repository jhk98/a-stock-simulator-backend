package com.myproject.stocksimulator.controller.dto.dailyPrice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 주식 종목 가격 생성 시 필요한 정보
@Getter
@NoArgsConstructor
public class DailyPricePostCommand {

    @NotNull(message = "stockId 는 필수입니다.")
    private Long stockId;

    @NotNull(message = "시가는 필수입니다.")
    private Long openPrice;

    @NotNull(message = "종가는 필수입니다.")
    private Long closePrice;

    @NotNull(message = "고가는 필수입니다.")
    private Long highPrice;

    @NotNull(message = "저가는 필수입니다.")
    private Long lowPrice;

    @NotNull(message = "거래량은 필수입니다.")
    private Long volume;
}