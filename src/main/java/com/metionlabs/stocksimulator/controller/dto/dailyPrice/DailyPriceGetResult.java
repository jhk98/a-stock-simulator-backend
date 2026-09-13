package com.metionlabs.stocksimulator.controller.dto.dailyPrice;

import com.metionlabs.stocksimulator.domain.DailyPrice;
import lombok.Getter;

import java.time.LocalDate;

// 주식 종목 가격 조회 시 반환할 정보
@Getter
public class DailyPriceGetResult {

    private final Long id;
    private final Long stockId;
    private final Long openPrice;
    private final Long closePrice;
    private final Long highPrice;
    private final Long lowPrice;
    private final Long volume;
    private final LocalDate priceDate;

    // 엔티티를 전달받아서 DTO객체로 변환하는 생성자
    private DailyPriceGetResult(DailyPrice dailyPrice) {
        this.id = dailyPrice.getId();
        this.stockId = dailyPrice.getStock() != null ? dailyPrice.getStock().getId() : null;
        this.openPrice = dailyPrice.getOpenPrice();
        this.closePrice = dailyPrice.getClosePrice();
        this.highPrice = dailyPrice.getHighPrice();
        this.lowPrice = dailyPrice.getLowPrice();
        this.volume = dailyPrice.getVolume();
        this.priceDate = dailyPrice.getPriceDate();
    }

    // 엔티티로부터 GetResult DTO를 생성하는 메서드
    public static DailyPriceGetResult from(DailyPrice dailyPrice) {
        return new DailyPriceGetResult(dailyPrice);
    }
}