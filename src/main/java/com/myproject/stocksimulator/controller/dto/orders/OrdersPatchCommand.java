package com.myproject.stocksimulator.controller.dto.orders;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 주문 수정할 수 있는 정보
@Getter
@NoArgsConstructor
public class OrdersPatchCommand {

    @Positive(message = "주문 가격은 1 이상이어야 합니다.")
    private Long orderPrice;

    @Positive(message = "주문 수량은 1 이상이어야 합니다.")
    private int quantity;
}