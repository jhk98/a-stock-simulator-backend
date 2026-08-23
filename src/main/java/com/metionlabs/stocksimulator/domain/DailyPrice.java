package com.metionlabs.stocksimulator.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="a_stock_daily_price")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
public class DailyPrice {

    // 주식 종목 가격 테이블 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false)
    private Long id;

    // 주식 아이디
    @ManyToOne
    @JoinColumn(name="stock_id", nullable = false)
    private Stock stock;

    // 시가
    @Column(name="open_price", nullable = false)
    private Long openPrice;

    // 종가
    @Column(name="close_price", nullable = false)
    private Long closePrice;

    // 고가
    @Column(name="high_price", nullable = false)
    private Long highPrice;

    // 저가
    @Column(name="low_price", nullable = false)
    private Long lowPrice;

    // 거래량
    @Column(name="volume", nullable = false)
    private Long volume;

    // 가격 기록 시간
    @Column(name="report_time", nullable = false)
    private LocalDateTime reportTime;

    protected DailyPrice() {
    }

}