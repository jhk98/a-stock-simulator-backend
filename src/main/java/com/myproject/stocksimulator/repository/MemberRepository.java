package com.myproject.stocksimulator.repository;

import com.myproject.stocksimulator.domain.member.Member;
import com.myproject.stocksimulator.domain.member.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Member Entity의 DB 접근을 담당하는 Repository
 *
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 사용할 수 있다.
 *
 * 별도의 SQL을 직접 작성하지 않아도 Spring Data JPA의 메서드 이름 규칙을 이용하여 필요한 조회 및 존재 여부 확인 기능을 정의할 수 있다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 로그인 아이디를 이용하여 회원을 조회한다.
     *
     * 해당 로그인 아이디의 회원이 존재할 수도 있고 존재하지 않을 수도 있으므로 Optional<Member> 타입으로 반환한다.
     *
     * @param loginId   조회할 회원의 로그인 아이디
     * @return          해당 로그인 아이디를 가진 회원
     */
    Optional<Member> findByLoginId(String loginId);

    /**
     * 동일한 로그인 아이디를 사용하는 회원이 존재하는지 확인한다.
     *
     * 회원가입 시 로그인 아이디 중복 검사를 위해 사용한다.
     *
     * @param loginId   중복 여부를 확인할 로그인 아이디
     * @return          로그인 아이디 존재 여부
     */
    boolean existsByLoginId(String loginId);

    /**
     * 동일한 이메일을 사용하는 회원이 존재하는지 확인한다.
     *
     * @param email 중복 여부를 확인할 이메일
     * @return      이메일 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 동일한 전화번호를 사용하는 회원이 존재하는지 확인한다.
     *
     * @param phoneNumber   중복 여부를 확인할 전화번호
     * @return              전화번호 존재 여부
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 회원 ID와 회원 상태를 조건으로 회원을 조회한다.
     *
     * 일반 회원 기능에서는 ACTIVE 상태의 회원만 조회하기 위해 사용한다.
     *
     * @param id        조회할 회원의 PK
     * @param status    조회할 회원 상태
     * @return          조건에 해당하는 회원
     */
    Optional<Member> findByIdAndStatus(Long id, MemberStatus status);

    /**
     * 특정 회원 상태이면서 기준 시간 이전에 탈퇴한 회원을 조사한다.
     *
     * 회원 탈퇴 후 30일이 지난 회원을 조회하여 실제 DB에서 삭제하기 위해 사용한다.
     *
     * @param status        조회할 회원 상태
     * @param withdrawnAt   탈퇴 일시의 기준 시간
     * @return              조건에 해당하는 회원 목록
     */
    List<Member> findByStatusAndWithdrawnAtLessThanEqual(MemberStatus status, LocalDateTime withdrawnAt);
}