package com.myproject.stocksimulator.domain;

import com.myproject.stocksimulator.domain.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Table(name="a_stock_account")
@Entity
@Getter
@Builder
@AllArgsConstructor
public class Account {

    // 계좌 테이블 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 멤버 아이디
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    // 계좌번호
    @Column(name = "account_num", nullable = false, unique = true)
    private String accountNum;

    // 예수금
    @Column(name = "deposit", nullable = false)
    private Long deposit;

    // 생성일
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 수정일
    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    // JPA가 DB 데이터를 엔티티 객체로 생성할 때 사용하는 기본 생성자
    // 외부에서 빈 객체를 직접 생성하지 못하도록 protected로 선언
    protected Account() {
    }

    /**
     * 계좌 정보를 부분 수정한다.
     *
     * <p>⭐ setter 를 열지 않고 이 메서드를 두는 이유:
     * <ul>
     *   <li>setter 를 열면 <b>아무나 아무 필드나</b> 바꿀 수 있다. {@code id} 나
     *       {@code createdAt} 처럼 절대 바뀌면 안 되는 값까지 열린다</li>
     *   <li>"값을 바꾸면 {@code modifiedAt} 도 갱신한다" 는 규칙을 <b>여기 한 곳</b>에만
     *       둘 수 있다. 호출부마다 적으면 언젠가 한 군데를 빠뜨리고, 조용히 어긋난다</li>
     * </ul>
     *
     * <p>null 인 인자는 "안 바꾼다" 는 뜻이다(PATCH 의미).
     * 그래서 예수금만 바꾸고 싶으면 accountNum 에 null 을 넘기면 된다.
     */
    // 계좌 정보 수정 메서드
    public void update(String accountNum, Long deposit) {
        if(accountNum != null) {
            this.accountNum = accountNum;
        }
        if(deposit != null) {
            this.deposit = deposit;
        }
        this.modifiedAt = LocalDateTime.now();
    }
}