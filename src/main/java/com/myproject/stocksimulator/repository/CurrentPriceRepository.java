package com.myproject.stocksimulator.repository;

import com.myproject.stocksimulator.domain.CurrentPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrentPriceRepository extends JpaRepository<CurrentPrice, Long> {

    // 주식 ID로 주식 현재가 조회
    Optional<CurrentPrice> findByStockId(Long stockId);

    // 주식 현재가에 주식이 있는지 확인
    boolean existsByStockId(Long stockId);
}