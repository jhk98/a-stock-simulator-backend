package com.metionlabs.stocksimulator.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Table(name="a_stock_current_price")
@Entity
@Getter
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

    // JPA가 DB 데이터를 엔티티 객체로 생성할 때 사용하는 기본 생성자
    // 외부에서 빈 객체를 직접 생성하지 못하도록 protected로 선언
    protected CurrentPrice() {
    }

    // 주식 현재가 수정 메서드
    public void update(Long currentPrice,
                       Long changePrice,
                       Double changeRate,
                       Long currentVolume) {
        if(currentPrice != null) {
            this.currentPrice = currentPrice;
        }
        if(changePrice != null) {
            this.changePrice = changePrice;
        }
        if(changeRate != null) {
            this.changeRate = changeRate;
        }
        if(currentVolume != null) {
            this.currentVolume = currentVolume;
        }
        this.updatedAt = LocalDateTime.now();
    }
}