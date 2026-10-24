package com.myproject.stocksimulator.stock.entity.currentPrice;

import com.myproject.stocksimulator.stock.entity.stock.Stock;

import jakarta.persistence.*;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 종목별 마지막 수집 시세
 *
 * 종목 목록의 가격 정렬과 보유자산 평가에 사용한다.
 * updatedAt은 시세 수집 시각이며 시장 체결 시각과는 다르다.
 */
@Entity
@Table(
        name = "a_stock_current_price",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_current_price_stock_id",
                        columnNames = "stock_id"
                )
        }
)
@Getter
public class CurrentPrice {

    // 최신 시세 테이블 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주식 ID
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "stock_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_current_price_stock")
    )
    private Stock stock;

    // 현재 가격
    @Column(name = "current_price", nullable = false)
    private Long currentPrice;

    // 전일 대비 가격 변화
    @Column(name = "change_price", nullable = false)
    private Long changePrice;

    // 전일 대비 등락률
    @Column(
            name = "change_rate",
            nullable = false,
            precision = 10,
            scale = 4
    )
    private BigDecimal changeRate;

    // 당일 누적 거래량
    @Column(name = "current_volume", nullable = false)
    private Long currentVolume;

    // 시세 수집 시각
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA 기본 생성자
     *
     * <p>
     * Hibernate가 DB 조회 결과로 Entity를 생성할 때 사용한다.
     * 외부에서 직접 호출하지 못하도록 protected로 제한한다.
     * </p>
     */
    protected CurrentPrice() {
    }

    /**
     * 최초 수집한 시세 행을 생성한다.
     * updatedAt은 호출부에서 한국 시각 기준으로 전달한다.
     */
    @Builder
    private CurrentPrice(
            Stock stock,
            Long currentPrice,
            Long changePrice,
            BigDecimal changeRate,
            Long currentVolume,
            LocalDateTime updatedAt
    ) {
        this.stock = Objects.requireNonNull(stock, "종목이 필요합니다.");

        refresh(
                currentPrice,
                changePrice,
                changeRate,
                currentVolume,
                updatedAt
        );
    }

    /**
     * 새로 수집한 시세로 전체 값을 갱신한다.
     * 부분 갱신으로 서로 다른 수집 시점의 값이 섞이지 않도록 한다.
     */
    public void refresh(
            Long currentPrice,
            Long changePrice,
            BigDecimal changeRate,
            Long currentVolume,
            LocalDateTime updatedAt
    ) {
        Objects.requireNonNull(currentPrice, "현재가가 필요합니다.");
        Objects.requireNonNull(changePrice, "전일 대비 가격 변화가 필요합니다.");
        Objects.requireNonNull(changeRate, "등락률이 필요합니다.");
        Objects.requireNonNull(currentVolume, "거래량이 필요합니다.");
        Objects.requireNonNull(updatedAt, "시세 수집 시각이 필요합니다.");

        if (currentVolume < 0) {
            throw new IllegalArgumentException("거래량은 음수가 될 수 없습니다.");
        }

        BigDecimal normalizedRate = changeRate.setScale(
                4,
                RoundingMode.HALF_UP
        );

        if (normalizedRate.precision() > 10) {
            throw new IllegalArgumentException(
                    "등락률이 저장 가능한 범위를 벗어났습니다."
            );
        }

        this.currentPrice = currentPrice;
        this.changePrice = changePrice;
        this.changeRate = normalizedRate;
        this.currentVolume = currentVolume;
        this.updatedAt = updatedAt.withNano(0);
    }
}