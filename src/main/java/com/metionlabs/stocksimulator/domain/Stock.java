package com.metionlabs.stocksimulator.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Table(name="a_stock_stock")
@Entity
@Getter
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

    // JPA가 DB 데이터를 엔티티 객체로 생성할 때 사용하는 기본 생성자
    // 외부에서 빈 객체를 직접 생성하지 못하도록 protected로 선언
    protected Stock () {
    }

    // 주식 정보 수정 메서드
    public void update(String stockCode, String stockName) {
        if(stockCode != null) {
            this.stockCode = stockCode;
        }
        if(stockName != null) {
            this.stockName = stockName;
        }
    }
}