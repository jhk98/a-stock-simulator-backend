package com.myproject.stocksimulator.stock.entity.stock;

/**
 * 지원하는 국내 주식 시장
 *
 * <p>
 * enum 이름은 DB 저장과 화면 시장 구분에 사용한다.
 * kiwoomMarketCode는 키움 종목 목록 조회에 사용한다.
 * </p>
 */
public enum StockType {

    KOSPI("0"),
    KOSDAQ("10");

    private final String kiwoomMarketCode;

    StockType(String kiwoomMarketCode) {
        this.kiwoomMarketCode = kiwoomMarketCode;
    }

    /**
     * StockSyncService가 시장별 종목을 수집할 때 사용한다.
     */
    public String kiwoomMarketCode() {
        return kiwoomMarketCode;
    }
}