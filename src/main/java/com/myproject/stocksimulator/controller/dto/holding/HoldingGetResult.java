package com.myproject.stocksimulator.controller.dto.holding;

import com.myproject.stocksimulator.domain.Holding;
import lombok.Getter;

import java.time.LocalDateTime;

// 회원 보유 주식 조회 시 반환할 정보
@Getter
public class HoldingGetResult {

    private final Long id;
    private final Long stockId;
    private final Long accountId;
    private final int quantity;
    private final Long avgPrice;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    // 엔티티를 전달받아서 DTO객체로 변환하는 생성자
    private HoldingGetResult(Holding holding) {
        this.id = holding.getId();
        this.stockId = holding.getStock() != null ? holding.getStock().getId() : null;
        this.accountId = holding.getAccount() != null ? holding.getAccount().getId() : null;
        this.quantity = holding.getQuantity();
        this.avgPrice = holding.getAvgPrice();
        this.createdAt = holding.getCreatedAt();
        this.modifiedAt = holding.getModifiedAt();
    }

    // 엔티티로부터 GetResult DTO를 생성하는 메서드
    public static HoldingGetResult from(Holding holding) {
        return new HoldingGetResult(holding);
    }
}