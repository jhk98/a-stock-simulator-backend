package com.myproject.stocksimulator.stock.entity.orders;

import com.myproject.stocksimulator.account.entity.Account;
import com.myproject.stocksimulator.stock.entity.stock.Stock;

import jakarta.persistence.*;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 성공한 모의 매수·매도 체결 기록
 *
 * requestId는 같은 주문의 재전송을 구분하기 위한 값이다.
 * 체결 후 가격·수량을 수정하는 메서드는 제공하지 않는다.
 */
@Entity
@Table(
        name = "a_stock_orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_orders_account_reqeust",
                        columnNames = {"account_id", "request_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_orders_account_id",
                        columnList = "account_id, id"
                )
        }
)
@Getter
public class Orders {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // 주문 테이블 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주식 ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "stock_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_orders_stock")
    )
    private Stock stock;

    // 계좌 ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_orders_account")
    )
    private Account account;

    // 주문 요청 식별자
    @Column(
            name = "request_id",
            nullable = false,
            length = 40,
            updatable = false
    )
    private String requestId;

    // 주문 타입
    @Enumerated(EnumType.STRING)
    @Column(
            name = "order_type",
            nullable = false,
            length = 20,
            columnDefinition = "varchar(20)",
            updatable = false
    )
    private OrderType orderType;

    // 주문 가격
    @Column(name = "order_price", nullable = false, updatable = false)
    private Long orderPrice;

    // 주문 수량
    @Column(name = "quantity", nullable = false, updatable = false)
    private int quantity;

    // 생성 시간
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * JPA 기본 생성자
     *
     * <p>
     * Hibernate가 DB 조회 결과로 Entity를 생성할 때 사용한다.
     * 외부에서 직접 호출하지 못하도록 protected로 제한한다.
     * </p>
     */
    protected Orders() {
    }

    /**
     * 체결 성공 후 주문 기록을 생성한다.
     * 잔액·보유수량 변경과 동일한 트랜잭션에서 저장한다.
     */
    @Builder
    private Orders(
            Stock stock,
            Account account,
            String requestId,
            OrderType orderType,
            Long orderPrice,
            int quantity
    ) {
        this.stock = Objects.requireNonNull(stock, "종목이 필요합니다.");
        this.account = Objects.requireNonNull(account, "계좌가 필요합니다.");
        this.orderType = Objects.requireNonNull(
                orderType,
                "매수·매도 구분이 필요합니다."
        );

        if (requestId == null
                || requestId.isBlank()
                || requestId.length() > 40) {
            throw new IllegalArgumentException(
                    "주문 요청 식별자는 1~40자여야 합니다."
            );
        }

        if (orderPrice == null || orderPrice <= 0) {
            throw new IllegalArgumentException("체결가격은 0보다 커야 합니다.");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("체결수량은 1주 이상이어야 합니다.");
        }

        this.requestId = requestId;
        this.orderPrice = orderPrice;
        this.quantity = quantity;
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now(SEOUL).withNano(0);
    }
}