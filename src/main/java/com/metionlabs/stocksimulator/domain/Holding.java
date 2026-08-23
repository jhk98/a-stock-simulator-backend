package com.metionlabs.stocksimulator.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="a_stock_holding")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Holding {

    // 보유 주식 테이블 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    // 주식 아이디
    @ManyToOne
    @JoinColumn(name="stock_id")
    private Stock stock;

    // 계좌 아이디
    @ManyToOne
    @JoinColumn(name="account_id")
    private Account account;

    // 보유 수량
    @Column(name="quantity")
    private int quantity;

    // 평균 금액
    @Column(name="avg_price")
    private Long avgPrice;

    // 생성일
    @Column(name="created_at")
    private LocalDateTime createdAt;

    // 수정일
    @Column(name="modified_at")
    private LocalDateTime modifiedAt;

    protected Holding() {
    }

}