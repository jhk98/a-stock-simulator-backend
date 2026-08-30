package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

// 주식 종목 저장소
public interface StockRepository extends JpaRepository<Stock, Long> {

    // 주식코드 존재 여부
    boolean existsByStockCode(String stockCode);

    // 주식이름 존재 여부
    boolean existsByStockName(String stockName);
}