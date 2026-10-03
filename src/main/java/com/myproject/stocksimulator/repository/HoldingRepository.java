package com.myproject.stocksimulator.repository;

import com.myproject.stocksimulator.domain.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 회원 보유 주식 저장소
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    // 계좌 ID로 회원 보유 주식 조회
    List<Holding> findByAccountId(Long accountId);

    // 특정 계좌가 특정 주식을 이미 보유하고 있는지 확인
    boolean existsByStockIdAndAccountId(Long stockId, Long accountId);

    /**
     * 특정 계좌에 속한 모든 보유 주식 데이터를 삭제한다.
     *
     * 회원 Hard Delete 시 Account를 삭제하기 전에 해당 Account를 참조하고 있는 Holding 데이터를 먼저 삭제하기 위해 사용한다.
     *
     * @param accountId 삭제할 보유 주식의 계좌 PK
     */
    void deleteByAccountId(Long accountId);
}