package com.myproject.stocksimulator.stock.repository;

import com.myproject.stocksimulator.stock.entity.Stock;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 종목 기본정보 저장소
 *
 * 종목 동기화와 거래대금 순위의 등록 종목 확인에 사용한다.
 * 검색·정렬 조회는 MarketService에서 JPA 쿼리로 처리한다.
 */
public interface StockRepository extends JpaRepository<Stock, Long> {
}