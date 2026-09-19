package com.myproject.stocksimulator.controller.dto.orders;

import com.myproject.stocksimulator.domain.orders.OrderStatus;
import com.myproject.stocksimulator.domain.orders.OrderType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 주문 생성 시 필요한 정보
@Getter
@NoArgsConstructor
public class OrdersPostCommand {

    @NotNull(message = "stockId 는 필수입니다.")
    private Long stockId;

    @NotNull(message = "accountId 는 필수입니다.")
    private Long accountId;

    @NotNull(message = "주문 종류는 필수입니다.")
    private OrderType orderType;

    @NotNull(message = "주문 가격은 필수입니다.")
    @Positive
    private Long orderPrice;

    @NotNull(message = "주문 상태는 필수입니다.")
    private OrderStatus orderStatus;

    @NotNull(message = "주문 수량은 필수입니다.")
    @Positive
    private int quantity;
}