package com.metionlabs.stocksimulator.domain;

import com.metionlabs.stocksimulator.domain.stock.Stock;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Table(name="a_stock_daily_price")
@Entity
@Getter
@Builder
@AllArgsConstructor
public class DailyPrice extends Object {

    // 주식 종목 가격 테이블 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 주식 아이디
    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // 시가
    @Column(name = "open_price", nullable = false)
    private Long openPrice;

    // 종가
    @Column(name = "close_price", nullable = false)
    private Long closePrice;

    // 고가
    @Column(name = "high_price", nullable = false)
    private Long highPrice;

    // 저가
    @Column(name = "low_price", nullable = false)
    private Long lowPrice;

    // 거래량
    @Column(name = "volume", nullable = false)
    private Long volume;

    // 가격 기록 시간
    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    // JPA가 DB 데이터를 엔티티 객체로 생성할 때 사용하는 기본 생성자
    // 외부에서 빈 객체를 직접 생성하지 못하도록 protected로 선언
    protected DailyPrice() {
    }

    // 주식 종목 가격 수정 메서드
    public void update(Long openPrice,
                       Long closePrice,
                       Long highPrice,
                       Long lowPrice,
                       Long volume) {
        if(openPrice != null) {
            this.openPrice = openPrice;
        }
        if(closePrice != null) {
            this.closePrice = closePrice;
        }
        if(highPrice != null) {
            this.highPrice = highPrice;
        }
        if(lowPrice != null) {
            this.lowPrice = lowPrice;
        }
        if(volume != null) {
            this.volume = volume;
        }
    }
}