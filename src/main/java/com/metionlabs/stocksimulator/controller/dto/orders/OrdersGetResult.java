package com.metionlabs.stocksimulator.controller.dto.orders;

import com.metionlabs.stocksimulator.domain.Orders;
import lombok.Getter;

import java.time.LocalDateTime;

// 주문 조회 응답
@Getter
public class OrdersGetResult {

    private final Long id;
    private final Long stockId;
    private final Long accountId;
    private final String orderType;
    private final Long orderPrice;
    private final String orderStatus;
    private final int quantity;
    private final LocalDateTime createdAt;

    private OrdersGetResult(Orders orders) {
        this.id = orders.getId();
        this.stockId = orders.getStock() != null ? orders.getStock().getId() : null;
        this.accountId = orders.getAccount() != null ? orders.getAccount().getId() : null;
        this.orderType = orders.getOrderType();
        this.orderPrice = orders.getOrderPrice();
        this.orderStatus = orders.getOrderStatus();
        this.quantity = orders.getQuantity();
        this.createdAt = orders.getCreatedAt();
    }

    public static OrdersGetResult from(Orders orders) {
        return new OrdersGetResult(orders);
    }
}