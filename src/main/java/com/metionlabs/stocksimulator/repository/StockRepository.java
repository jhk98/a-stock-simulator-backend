package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.stock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

// 주식 종목 저장소
public interface StockRepository extends JpaRepository<Stock, Long> {

    // 주식코드 중복 여부 확인
    boolean existsByStockCode(String stockCode);

    // 주식이름 중복 여부 확인
    boolean existsByStockName(String stockName);
}