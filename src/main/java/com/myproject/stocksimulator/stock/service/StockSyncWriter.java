package com.myproject.stocksimulator.stock.service;

import com.myproject.stocksimulator.stock.entity.stock.Stock;
import com.myproject.stocksimulator.stock.entity.stock.StockType;
import com.myproject.stocksimulator.stock.dto.StockSyncResult;
import com.myproject.stocksimulator.stock.repository.StockRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockSyncWriter {

    private final StockRepository stockRepository;

    public StockSyncWriter(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public record StockItem(
            String stockCode,
            String stockName,
            StockType stockType
    ) {
        public StockItem {
            if (stockCode == null || stockCode.isBlank()
                    || stockName == null || stockName.isBlank()
                    || stockType == null) {
                throw new IllegalArgumentException(
                        "종목 정보가 비어 있습니다."
                );
            }

            stockCode = stockCode.trim();
            stockName = stockName.trim();

            if (stockCode.length() > 20 || stockName.length() > 100) {
                throw new IllegalArgumentException(
                        "종목 정보 길이가 올바르지 않습니다."
                );
            }
        }
    }

    /**
     * 수집한 전체 목록을 하나의 트랜잭션으로 저장한다.
     */
    @Transactional
    public StockSyncResult saveStocks(List<StockItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "빈 목록은 동기화하지 않습니다."
            );
        }

        if (items.stream().anyMatch(item -> item == null)) {
            throw new IllegalArgumentException(
                    "종목 정보가 누락되었습니다."
            );
        }

        long distinctCount = items.stream()
                .map(StockItem::stockCode)
                .distinct()
                .count();

        if (distinctCount != items.size()) {
            throw new IllegalArgumentException(
                    "중복된 종목 코드가 있습니다."
            );
        }

        Map<String, Stock> existingStocks = new HashMap<>();

        for (Stock stock : stockRepository.findAll()) {
            existingStocks.put(stock.getStockCode(), stock);
        }

        Instant syncedAt = Instant.now()
                .truncatedTo(ChronoUnit.SECONDS);

        List<Stock> newStocks = new ArrayList<>();

        int updated = 0;
        int unchanged = 0;

        for (StockItem item : items) {
            Stock existing = existingStocks.get(item.stockCode());

            if (existing == null) {
                newStocks.add(
                        Stock.builder()
                                .stockCode(item.stockCode())
                                .stockName(item.stockName())
                                .stockType(item.stockType())
                                .lastSyncedAt(syncedAt)
                                .build()
                );
            } else {
                boolean changed = existing.refreshMetadata(
                        item.stockName(),
                        item.stockType(),
                        syncedAt
                );

                if (changed) {
                    updated++;
                } else {
                    unchanged++;
                }
            }
        }

        stockRepository.saveAll(newStocks);

        // 기존 엔티티 변경도 함께 DB에 반영한다.
        stockRepository.flush();

        // 목록에서 누락됐다는 이유만으로 종목을 삭제하지 않는다.
        return new StockSyncResult(
                items.size(),
                newStocks.size(),
                updated,
                unchanged,
                syncedAt
        );
    }
}