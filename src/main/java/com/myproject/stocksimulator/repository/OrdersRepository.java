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

    /**
     * 특정 계좌에 속한 모든 주문 데이터를 삭제한다.
     *
     * 회원 Hard Delete 시 Account를 삭제하기 전에 해당 Account를 참조하고 있는 Orders 데이터를 먼저 삭제하기 위해 사용한다.
     *
     * @param accountId 삭제할 주문 데이터의 계좌 PK
     */
    void deleteByAccountId(Long accountId);
}