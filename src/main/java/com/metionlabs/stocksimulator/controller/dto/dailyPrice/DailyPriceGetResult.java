package com.metionlabs.stocksimulator.controller.dto.dailyPrice;

import com.metionlabs.stocksimulator.domain.DailyPrice;
import lombok.Getter;

import java.time.LocalDateTime;

// 주식 종목 가격 조회 응답
@Getter
public class DailyPriceGetResult {

    private final Long id;
    private final Long stockId;
    private final Long openPrice;
    private final Long closePrice;
    private final Long highPrice;
    private final Long lowPrice;
    private final Long volume;
    private final LocalDateTime reportTime;

    private DailyPriceGetResult(DailyPrice dailyPrice) {
        this.id = dailyPrice.getId();
        this.stockId = dailyPrice.getStock() != null ? dailyPrice.getStock().getId() : null;
        this.openPrice = dailyPrice.getOpenPrice();
        this.closePrice = dailyPrice.getClosePrice();
        this.highPrice = dailyPrice.getHighPrice();
        this.lowPrice = dailyPrice.getLowPrice();
        this.volume = dailyPrice.getVolume();
        this.reportTime = dailyPrice.getReportTime();
    }

    public static DailyPriceGetResult from(DailyPrice dailyPrice) {
        return new DailyPriceGetResult(dailyPrice);
    }
}