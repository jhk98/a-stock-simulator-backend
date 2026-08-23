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
}