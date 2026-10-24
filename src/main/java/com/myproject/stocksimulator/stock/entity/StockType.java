package com.myproject.stocksimulator.stock.entity;

import java.util.Locale;

public enum StockType {

    KOSPI("0"),
    KOSDAQ("10");

    private final String kiwoomMarketCode;

    StockType(String kiwoomMarketCode) {
        this.kiwoomMarketCode = kiwoomMarketCode;
    }

    public String kiwoomMarketCode() {
        return kiwoomMarketCode;
    }

    public static StockType parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "시장 구분이 비어 있습니다."
            );
        }

        String normalized = value.trim()
                .toUpperCase(Locale.ROOT);

        try {
            return StockType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "market은 KOSPI 또는 KOSDAQ이어야 합니다."
            );
        }
    }
}