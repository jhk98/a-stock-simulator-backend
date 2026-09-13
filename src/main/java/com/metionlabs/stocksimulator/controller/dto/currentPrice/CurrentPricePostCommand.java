package com.metionlabs.stocksimulator.controller.dto.currentPrice;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class CurrentPricePostCommand {

    @NotNull(message = "stockId 는 필수입니다.")
    private Long stockId;

    @NotNull(message = "현재가는 필수입니다.")
    private Long currentPrice;

    @NotNull(message = "변동가는 필수입니다.")
    private Long changePrice;

    @NotNull(message = "변동 비율은 필수입니다.")
    private BigDecimal changeRate;

    @NotNull(message = "현재 거래량은 필수입니다.")
    private Long currentVolume;
}