package com.myproject.stocksimulator.kiwoom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// 이번 기능에 필요한 필드만 받습니다.
// 키움이 추가로 내려주는 필드는 무시합니다.
@JsonIgnoreProperties(ignoreUnknown = true)
public record KiwoomStockInfo(
        String code,
        String name,
        String marketCode,
        String marketName,
        String state,
        String lastPrice // 전일종가입니다. 실시간 현재가가 아닙니다.
) {
}