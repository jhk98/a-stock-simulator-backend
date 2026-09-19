package com.myproject.stocksimulator.domain;

import com.myproject.stocksimulator.domain.stock.Stock;
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
    @Column(name = "id")
    private Long id;

    // 주식 아이디
    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // 계좌 아이디
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // 보유 수량
    @Column(name = "quantity", nullable = false)
    private int quantity;

    // 평균 금액
    @Column(name = "avg_price", nullable = false)
    private Long avgPrice;

    // 생성일
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 수정일
    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    // JPA가 DB 데이터를 엔티티 객체로 생성할 때 사용하는 기본 생성자
    // 외부에서 빈 객체를 직접 생성하지 못하도록 protected로 선언
    protected Holding() {
    }

    // 회원 보유 주식 정보 수정 메서드
    public void update(Integer quantity, Long avgPrice) {
        if(quantity != null) {
            this.quantity = quantity;
        }
        if(avgPrice != null) {
            this.avgPrice = avgPrice;
        }
        this.modifiedAt = LocalDateTime.now();
    }
}