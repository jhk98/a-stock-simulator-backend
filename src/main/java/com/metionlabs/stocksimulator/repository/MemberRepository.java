package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 회원 저장소
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인 아이디로 조회 (중복검사에 사용)
    Optional<Member> findByLoginId(String loginId);

    // 이메일 존재 여부
    boolean existsByEmail(String email);

    // 전화번호 존재 여부
    boolean existsByPhoneNumber(String phoneNumber);
}