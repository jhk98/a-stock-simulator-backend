package com.metionlabs.stocksimulator.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    @Column(name="id")
    private Long id;

    // 멤버 아이디
    @OneToOne
    @JoinColumn(name="member_id")
    private Member member;

    // 계좌번호
    @Column(name="account_num")
    private String accountNum;

    // 예수금
    @Column(name="deposit")
    private Long deposit;

    // 생성일
    @Column(name="created_at")
    private LocalDateTime createdAt;

    // 수정일
    @Column(name="modified_at")
    private LocalDateTime modifiedAt;

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
    public void update(String accountNum, Long deposit) {
        if (accountNum != null) {
            this.accountNum = accountNum;
        }
        if (deposit != null) {
            this.deposit = deposit;
        }
        this.modifiedAt = LocalDateTime.now();
    }
}