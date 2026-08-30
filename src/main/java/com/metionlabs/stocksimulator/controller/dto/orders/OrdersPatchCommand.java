package com.metionlabs.stocksimulator.controller.dto.orders;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 주문 부분 수정 요청
@Getter
@NoArgsConstructor
public class OrdersPatchCommand {

    private String orderType;

    @PositiveOrZero(message = "주문 가격은 0 이상이어야 합니다.")
    private Long orderPrice;

    private String orderStatus;

    @PositiveOrZero(message = "주문 수량은 0 이상이어야 합니다.")
    private int quantity;
}