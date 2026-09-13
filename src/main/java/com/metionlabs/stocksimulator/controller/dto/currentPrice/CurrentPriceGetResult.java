package com.metionlabs.stocksimulator.controller.dto.currentPrice;

import com.metionlabs.stocksimulator.domain.CurrentPrice;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 주식 현재가 조회 응답
@Getter
public class CurrentPriceGetResult {

    private final Long id;
    private final Long stockId;
    private final Long currentPrice;
    private final Long changePrice;
    private final BigDecimal changeRate;
    private final Long currentVolume;
    private final LocalDateTime updatedAt;

    // 엔티티를 전달받아서 DTO객체로 변환하는 생성자
    private CurrentPriceGetResult(CurrentPrice currentPrice) {
        this.id = currentPrice.getId();
        this.stockId = currentPrice.getStock() != null ? currentPrice.getStock().getId() : null;
        this.currentPrice = currentPrice.getCurrentPrice();
        this.changePrice = currentPrice.getChangePrice();
        this.changeRate = currentPrice.getChangeRate();
        this.currentVolume = currentPrice.getCurrentVolume();
        this.updatedAt = currentPrice.getUpdatedAt();
    }

    // 엔티티로부터 GetResult DTO를 생성하는 메서드
    public static CurrentPriceGetResult from(CurrentPrice currentPrice) {
        return new CurrentPriceGetResult(currentPrice);
    }
}