package com.metionlabs.stocksimulator.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Table(name="a_stock_stock")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Stock {

    // 주식 종목 테이블 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    // 주식 코드
    @Column(name="stock_code")
    private String stockCode;

    // 주식 이름
    @Column(name="stock_name")
    private String stockName;

    // 주식 타입 (KOSPI / KOSDAQ)
    @Column(name="stock_type")
    private String stockType;

    protected Stock () {
    }

}