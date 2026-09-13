package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 회원 보유 주식 저장소
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    // 계좌 ID로 회원 보유 주식 조회
    List<Holding> findByAccountId(Long accountId);

    // 특정 계좌가 특정 주식을 이미 보유하고 있는지 확인
    boolean existsByStockIdAndAccountId(Long stockId, Long accountId);
}