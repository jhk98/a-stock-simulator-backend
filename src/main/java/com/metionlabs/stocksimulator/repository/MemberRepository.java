package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 회원 저장소
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인 아이디로 조회
    Optional<Member> findByLoginId(String loginId);

    // 로그인 아이디 중복 여부 확인
    boolean existsByLoginId(String loginId);

    // 이메일 중복 여부 확인
    boolean existsByEmail(String email);

    // 전화번호 중복 여부 확인
    boolean existsByPhoneNumber(String phoneNumber);
}