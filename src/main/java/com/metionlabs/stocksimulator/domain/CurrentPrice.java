package com.metionlabs.stocksimulator.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="a_stock_current_price")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
public class CurrentPrice {

    // 주식 현재가 테이블 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false)
    private Long id;

    // 주식 아이디
    @OneToOne
    @JoinColumn(name="stock_id", nullable = false)
    private Stock stock;

    // 현재가
    @Column(name="current_price" ,nullable = false)
    private Long currentPrice;

    // 변동가
    @Column(name="change_price", nullable = false)
    private Long changePrice;

    // 변동 비율
    @Column(name="change_rate", nullable = false)
    private double changeRate;

    // 현재 거래량
    @Column(name="current_volume", nullable = false)
    private Long currentVolume;

    // 마지막으로 갱신된 시간
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CurrentPrice() {
    }

}