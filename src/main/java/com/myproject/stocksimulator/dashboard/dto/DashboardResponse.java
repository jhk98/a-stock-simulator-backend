package com.myproject.stocksimulator.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 메인 화면 응답
 *
 * <p>
 * 금액 계산은 서버에서 수행한다.
 * 필요한 시세가 없으면 평가금액과 손익은 null로 반환한다.
 * </p>
 */
public record DashboardResponse(
        long initialBalance,
        AccountView account,
        List<HoldingView> holdings,
        List<StockView> featuredStocks,
        boolean valuationComplete,
        BigDecimal holdingValue,
        BigDecimal totalAssets,
        BigDecimal totalProfit
) {

    /**
     * 로그인한 회원의 모의계좌
     */
    public record AccountView(
            Long id,
            String accountNum,
            Long deposit,
            LocalDateTime createdAt
    ) {
    }

    /**
     * 동일 종목의 보유 행들을 합산한 결과
     *
     * <p>
     * quantity는 여러 INT 수량을 합산할 수 있도록 long을 사용한다.
     * avgPrice는 합산 매입금액 / 합산 수량이다.
     * </p>
     */
    public record HoldingView(
            String stockCode,
            String stockName,
            String market,
            long quantity,
            BigDecimal avgPrice,
            Long currentPrice,
            BigDecimal marketValue,
            BigDecimal unrealizedProfit,
            LocalDateTime priceUpdatedAt
    ) {
    }

    /**
     * DB에 저장된 종목 및 최신 저장 시세
     */
    public record StockView(
            String stockCode,
            String stockName,
            String market,
            Long currentPrice,
            BigDecimal changeRate,
            Long volume,
            LocalDateTime priceUpdatedAt
    ) {
    }
}