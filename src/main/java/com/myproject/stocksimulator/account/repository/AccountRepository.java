package com.myproject.stocksimulator.account.repository;

import com.myproject.stocksimulator.account.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 모의계좌 저장소
 *
 * 회원은 하나의 계좌만 가질 수 있다.
 * 잔액 변경과 중복 개설 방지는 서비스에서 처리한다.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    // 로그인한 회원의 모의계좌를 조회한다.
    Optional<Account> findByMemberId(Long memberId);
}