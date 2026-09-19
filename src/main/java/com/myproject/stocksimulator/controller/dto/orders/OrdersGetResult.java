package com.myproject.stocksimulator.controller.dto.orders;

import com.myproject.stocksimulator.domain.orders.OrderStatus;
import com.myproject.stocksimulator.domain.orders.OrderType;
import com.myproject.stocksimulator.domain.orders.Orders;
import lombok.Getter;

import java.time.LocalDateTime;

// 주문 조회 시 반환할 정보
@Getter
public class OrdersGetResult {

    private final Long id;
    private final Long stockId;
    private final Long accountId;
    private final OrderType orderType;
    private final Long orderPrice;
    private final OrderStatus orderStatus;
    private final int quantity;
    private final LocalDateTime createdAt;

    // 엔티티를 전달받아서 DTO객체로 변환하는 생성자
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

    // 엔티티로부터 GetResult DTO를 생성하는 메서드
    public static OrdersGetResult from(Orders orders) {
        return new OrdersGetResult(orders);
    }
}