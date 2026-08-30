package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 주문 저장소
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    // 주식 ID로 주문 조회
    Optional<Orders> findByStockId(Long stockId);

    // 계좌 ID로 주문 조회
    Optional<Orders> findByAccountId(Long accoutId);

    // 특정 계좌가 이미 특정 주문을 보유하고 있는지 확인
    boolean existsByStockIdAndAccountId(Long stockId, Long accountId);
}