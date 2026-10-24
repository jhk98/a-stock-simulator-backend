package com.myproject.stocksimulator.stock.entity.holding;

import com.myproject.stocksimulator.account.entity.Account;
import com.myproject.stocksimulator.stock.entity.stock.Stock;

import jakarta.persistence.*;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 계좌가 보유한 주식의 수량과 평균 매입가
 *
 * <p>
 * 확정 DB에는 계좌·종목 조합의 UNIQUE 제약이 없다.
 * 같은 종목의 여러 행 처리와 동시 매매 제어는 서비스에서 담당한다.
 * </p>
 */
@Entity
@Table(name = "a_stock_holding")
@Getter
public class Holding {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // 보유 주식 테이블 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주식 ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "stock_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_holding_stock")
    )
    private Stock stock;

    // 계좌 ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_holding_account")
    )
    private Account account;

    // 보유 수량
    @Column(name = "quantity", nullable = false)
    private int quantity;

    // 주당 평균 매입가
    @Column(
            name = "avg_price",
            nullable = false,
            precision = 19,
            scale = 6
    )
    private BigDecimal avgPrice;

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
    protected Holding() {
    }

    /**
     * 새 보유 행을 생성한다.
     * 식별자와 저장 시각은 외부에서 지정하지 않는다.
     */
    @Builder
    private Holding(
            Stock stock,
            Account account,
            int quantity,
            BigDecimal avgPrice
    ) {
        this.stock = Objects.requireNonNull(stock, "종목이 필요합니다.");
        this.account = Objects.requireNonNull(account, "계좌가 필요합니다.");

        changePosition(quantity, avgPrice);
    }

    /**
     * 서비스에서 계산한 최종 수량과 평균 매입가를 반영한다.
     *
     * <p> 전량 매도일 때는 수량을 0으로 바꾸지 않고 서비스에서 해당 보유 행을 삭제해야 한다. </p>
     */
    public void changePosition(int quantity, BigDecimal avgPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("보유수량은 1주 이상이어야 합니다.");
        }

        if (avgPrice == null) {
            throw new IllegalArgumentException("평균 매입가가 필요합니다.");
        }

        BigDecimal normalizedPrice = avgPrice.setScale(
                6,
                RoundingMode.HALF_UP
        );

        if (normalizedPrice.signum() <= 0
                || normalizedPrice.precision() > 19) {
            throw new IllegalArgumentException(
                    "평균 매입가가 저장 가능한 범위를 벗어났습니다."
            );
        }

        this.quantity = quantity;
        this.avgPrice = normalizedPrice;
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now(SEOUL).withNano(0);
        createdAt = now;
        modifiedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        modifiedAt = LocalDateTime.now(SEOUL).withNano(0);
    }
}