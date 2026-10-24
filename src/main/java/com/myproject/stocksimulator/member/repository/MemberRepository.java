package com.myproject.stocksimulator.member.repository;

import com.myproject.stocksimulator.member.entity.Member;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 회원 데이터 조회와 저장
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 기존 로그인 기능에서 사용한다.
    Optional<Member> findByLoginId(String loginId);

    // 기존 회원가입 중복 확인에서 사용한다.
    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    /**
     * 개인정보 변경 시 회원 행을 잠근다.
     * 반드시 서비스의 트랜잭션 안에서 호출한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :memberId")
    Optional<Member> findByIdForUpdate(
            @Param("memberId") Long memberId
    );
}