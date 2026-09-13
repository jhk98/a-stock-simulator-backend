package com.metionlabs.stocksimulator.controller.dto.holding;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 회원 보유 주식을 수정할 수 있는 정보
@Getter
@NoArgsConstructor
public class HoldingPatchCommand {

    @PositiveOrZero(message = "보유 수량은 0 이상이어야 합니다.")
    private int quantity;

    @PositiveOrZero(message = "평균 금액은 0 이상이어야 합니다.")
    private Long avgPrice;
}