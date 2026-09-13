package com.metionlabs.stocksimulator.domain.orders;

import com.metionlabs.stocksimulator.domain.Account;
import com.metionlabs.stocksimulator.domain.stock.Stock;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Table(name="a_stock_orders")
@Entity
@Getter
@Builder
@AllArgsConstructor
public class Orders {

    // 주문 테이블 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 주식 아이디
    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // 계좌 아이디
    @ManyToOne
    @JoinColumn(name = "account_id" , nullable = false)
    private Account account;

    // 주문 종류 (BUY / SELL)
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    // 주문 가격
    @Column(name = "order_price", nullable = false)
    private Long orderPrice;

    // 주문 상태 (COMPLETE / CANCEL)
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    // 주문 수량
    @Column(name = "quantity", nullable = false)
    private int quantity;

    // 생성일
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // JPA가 DB 데이터를 엔티티 객체로 생성할 때 사용하는 기본 생성자
    // 외부에서 빈 객체를 직접 생성하지 못하도록 protected로 선언
    protected Orders() {
    }

    // 주문 정보 수정 메서드
    public void update(Long orderPrice, Integer quantity) {
        if(orderPrice != null) {
            this.orderPrice = orderPrice;
        }
        if(quantity != null) {
            this.quantity = quantity;
        }
    }
}