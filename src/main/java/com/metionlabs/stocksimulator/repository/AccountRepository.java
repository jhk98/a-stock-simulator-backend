package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 계좌 저장소.
 *
 * <p>이 계층이 하는 일은 <b>쿼리뿐</b>이다. 규칙 판단(중복이면 거절, 잔액이 음수면 거절 등)은
 * 여기가 아니라 Service 가 한다. 그래야 규칙이 한 곳에만 있다.
 *
 * <p>메서드 이름만 규칙대로 지으면 Spring Data JPA 가 구현체를 만들어준다.
 * {@code findBy} + 필드명 + 조건. 그래서 SQL 을 안 써도 된다.
 *
 * <p>⚠️ 반환 타입에 {@code Optional} 을 쓰는 이유: "없을 수 있다" 를 <b>타입으로</b> 알려주기
 * 위해서다. {@code Account} 를 그대로 반환하면 호출부가 null 체크를 잊어도 컴파일이 통과하고,
 * 실제 운영에서 NPE 로 터진다. Optional 이면 컴파일러가 처리를 강제한다.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    /** 계좌번호로 조회. 중복 검사에 쓴다. */
    Optional<Account> findByAccountNum(String accountNum);

    /**
     * 계좌번호 존재 여부.
     *
     * <p>존재 확인만 필요할 땐 {@code findBy...isPresent()} 보다 이쪽이 낫다.
     * 엔티티 전체를 읽어오지 않고 {@code SELECT 1} 수준으로 끝난다.
     */
    boolean existsByAccountNum(String accountNum);

    /**
     * 회원 ID 로 계좌 조회.
     *
     * <p>{@code Account.member} 가 {@code @OneToOne} 이므로 회원당 계좌는 하나다.
     * 필드가 {@code member} 이고 그 안의 {@code id} 를 타고 들어가므로 {@code findByMemberId}
     * 로 쓴다 (밑줄 없이 카멜케이스로 이어 붙이면 JPA 가 알아서 연관을 따라간다).
     */
    Optional<Account> findByMemberId(Long memberId);

    /** 회원에게 이미 계좌가 있는지. 1인 1계좌 규칙 검사용. */
    boolean existsByMemberId(Long memberId);
}