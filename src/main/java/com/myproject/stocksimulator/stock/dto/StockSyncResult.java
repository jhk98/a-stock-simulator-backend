package com.myproject.stocksimulator.stock.dto;

import java.time.Instant;

/**
 * 종목 동기화 결과
 *
 * <p>
 * unchangedCount:
 * 종목명과 시장이 동일했던 종목 수.
 * 해당 종목도 마지막 동기화 시각은 갱신한다.
 * </p>
 */
public record StockSyncResult(
        int totalCount,
        int insertedCount,
        int updatedCount,
        int unchangedCount,
        Instant syncedAt
) {
}