package com.metionlabs.stocksimulator.controller.dto.orders;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 주문 생성 요청
@Getter
@NoArgsConstructor
public class OrdersPostCommand {

    @NotNull(message = "stockId 는 필수입니다.")
    private Long stockId;

    @NotNull(message = "accountId 는 필수입니다.")
    private Long accountId;

    @NotBlank(message = "주문 종류는 필수입니다.")
    private String orderType;

    @NotNull(message = "주문 가격은 필수입니다.")
    @PositiveOrZero(message = "주문 가격은 0 이상이어야 합니다.")
    private Long orderPrice;

    @NotBlank(message = "주문 상태는 필수입니다.")
    private String orderStatus;

    @NotNull(message = "주문 수량은 필수입니다.")
    @PositiveOrZero(message = "주문 수량은 0 이상이어야 합니다.")
    private int quantity;
}