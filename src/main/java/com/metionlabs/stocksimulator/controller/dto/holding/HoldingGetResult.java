package com.metionlabs.stocksimulator.controller.dto.holding;

import com.metionlabs.stocksimulator.domain.Holding;
import lombok.Getter;

import java.time.LocalDateTime;

// 회원 보유 주식 조회 응답
@Getter
public class HoldingGetResult {

    private final Long id;
    private final Long stockId;
    private final Long accountId;
    private final int quantity;
    private final Long avgPrice;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    private HoldingGetResult(Holding holding) {
        this.id = holding.getId();
        this.stockId = holding.getStock() != null ? holding.getStock().getId() : null;
        this.accountId = holding.getAccount() != null ? holding.getAccount().getId() : null;
        this.quantity = holding.getQuantity();
        this.avgPrice = holding.getAvgPrice();
        this.createdAt = holding.getCreatedAt();
        this.modifiedAt = holding.getModifiedAt();
    }

    public static HoldingGetResult from(Holding holding) {
        return new HoldingGetResult(holding);
    }
}