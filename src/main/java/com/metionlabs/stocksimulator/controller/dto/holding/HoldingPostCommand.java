package com.metionlabs.stocksimulator.controller.dto.holding;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 회원 보유 주식 생성 시 필요한 정보
@Getter
@NoArgsConstructor
public class HoldingPostCommand {

    @NotNull(message = "stockId는 필수입니다.")
    private Long stockId;

    @NotNull(message = "accountId는 필수입니다.")
    private Long accountId;

    @NotNull(message = "보유 수량은 필수입니다.")
    @PositiveOrZero(message = "보유 수량은 0 이상이어야 합니다.")
    private int quantity;

    @NotNull(message = "평균 금액은 필수입니다.")
    @PositiveOrZero(message = "평균 금액은 0 이상이어야 합니다.")
    private Long avgPrice;
}