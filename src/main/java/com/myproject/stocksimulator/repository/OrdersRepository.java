package com.myproject.stocksimulator.repository;

import com.myproject.stocksimulator.domain.orders.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 주문 저장소
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    // 주식 ID로 주문 조회
    List<Orders> findByStockId(Long stockId);

    // 계좌 ID로 주문 조회
    List<Orders> findByAccountId(Long accoutId);
}