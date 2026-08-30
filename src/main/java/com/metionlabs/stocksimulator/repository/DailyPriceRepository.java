package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.DailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DailyPriceRepository extends JpaRepository<DailyPrice, Long> {

    // 주식 ID로 주식 종목 가격 조회
    Optional<DailyPrice> findByStockId(Long stockId);

    // 주식 종목 가격에 주식이 있는지 확인
    boolean existsByStockId(Long stockId);
}
