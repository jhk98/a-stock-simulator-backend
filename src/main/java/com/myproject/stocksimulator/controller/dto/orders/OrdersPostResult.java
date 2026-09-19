package com.myproject.stocksimulator.controller.dto.orders;

import lombok.Getter;

// 주문 생성 후 생성된 주문 정보 반환
@Getter
public class OrdersPostResult {

    private final Long ordersId;

    public OrdersPostResult(Long ordersId) {
        this.ordersId = ordersId;
    }
}