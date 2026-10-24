package com.myproject.stocksimulator.account.entity;

import com.myproject.stocksimulator.member.entity.Member;

import jakarta.persistence.*;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 회원의 모의계좌
 *
 * <p>
 * deposit: 주식 평가금액을 제외한 사용 가능한 현금
 * resetVersion: 계좌를 리셋할 때 증가하는 업무용 구분값
 * </p>
 */
@Entity
@Table(
        name = "a_stock_account",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_account_member_id",
                        columnNames = "member_id"
                ),
                @UniqueConstraint(
                        name = "uk_account_num",
                        columnNames = "account_num"
                )
        }
)
@Getter
public class Account {

    public static final long INITIAL_BALANCE = 100_000_000L;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // 계좌 테이블 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 멤버 테이블 ID
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_account_member")
    )
    private Member member;

    // 계좌 번호
    @Column(
            name = "account_num",
            nullable = false,
            length = 30,
            updatable = false
    )
    private String accountNum;

    // 사용 가능한 잔액
    @Column(name = "deposit", nullable = false)
    private Long deposit;

    // 계좌 리셋 시 1 증가
    @Column(name = "reset_version", nullable = false)
    private long resetVersion = 0L;

    // 생성 시간
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 변경 시간
    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    /**
     * JPA 기본 생성자
     *
     * <p>
     * Hibernate가 DB 조회 결과로 Entity를 생성할 때 사용한다.
     * 외부에서 직접 호출하지 못하도록 protected로 제한한다.
     * </p>
     */
    protected Account() {
    }

    /**
     * 계좌 생성
     *
     * <p>
     * 기존 DashboardService의 builder 호출과 호환된다.
     * 초기 잔액을 생략하면 1억 원으로 설정한다.
     * </p>
     */
    @Builder
    private Account(
            Member member,
            String accountNum,
            Long deposit,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt
    ) {
        this.member = Objects.requireNonNull(member, "회원이 필요합니다.");

        if (accountNum == null
                || accountNum.isBlank()
                || accountNum.length() > 30) {
            throw new IllegalArgumentException(
                    "계좌번호는 1~30자로 입력해주세요."
            );
        }

        long initialDeposit = deposit == null
                ? INITIAL_BALANCE
                : deposit;

        if (initialDeposit < 0) {
            throw new IllegalArgumentException("잔액은 음수가 될 수 없습니다.");
        }

        this.accountNum = accountNum;
        this.deposit = initialDeposit;
        this.createdAt = createdAt == null
                ? null
                : createdAt.withNano(0);
        this.modifiedAt = modifiedAt == null
                ? null
                : modifiedAt.withNano(0);
    }

    /**
     * 매수금액을 현금 잔액에서 차감한다.
     * 서비스의 트랜잭션과 계좌 잠금 안에서 호출해야 한다.
     */
    public void withdraw(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다.");
        }

        if (deposit < amount) {
            throw new IllegalArgumentException("예수금이 부족합니다.");
        }

        this.deposit = Math.subtractExact(deposit, amount);
    }

    /**
     * 매도금액을 현금 잔액에 더한다.
     */
    public void credit(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("입금 금액은 0보다 커야 합니다.");
        }

        this.deposit = Math.addExact(deposit, amount);
    }

    /**
     * 현금을 1억 원으로 복원하고 리셋 구분값을 증가시킨다.
     *
     * <p>
     * 보유종목·주문 삭제는 서비스에서 같은 트랜잭션으로 처리한다.
     * 이 메서드만 호출하는 것으로 전체 리셋이 완료되지는 않는다.
     * </p>
     */
    public void reset() {
        long nextVersion = Math.incrementExact(resetVersion);

        this.deposit = INITIAL_BALANCE;
        this.resetVersion = nextVersion;
    }

    // DB 기본값에 의존하지 않고 JPA 저장 시각을 직접 설정한다.
    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now(SEOUL).withNano(0);

        if (createdAt == null) {
            createdAt = now;
        }

        if (modifiedAt == null) {
            modifiedAt = now;
        }
    }

    @PreUpdate
    private void onUpdate() {
        modifiedAt = LocalDateTime.now(SEOUL).withNano(0);
    }
}