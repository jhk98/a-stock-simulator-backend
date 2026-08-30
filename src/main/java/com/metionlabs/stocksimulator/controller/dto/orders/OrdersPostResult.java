package com.metionlabs.stocksimulator.controller.dto.orders;

import lombok.Getter;

// 주문 생성 응답
@Getter
public class OrdersPostResult {

    private final Long ordersId;

    public OrdersPostResult(Long ordersId) {
        this.ordersId = ordersId;
    }
}