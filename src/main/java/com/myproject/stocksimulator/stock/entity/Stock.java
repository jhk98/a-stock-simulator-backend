package com.myproject.stocksimulator.stock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(name = "a_stock_stock",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_code",
                        columnNames = "stock_code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_stock_market_code",
                        columnList = "stock_type, stock_code"
                ),
                @Index(
                        name = "idx_stock_name",
                        columnList = "stock_name"
                )
        }
)
@Getter
@Builder
@AllArgsConstructor
public class Stock {

    // 주식 테이블 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 주식 코드
    @Column(name = "stock_code", nullable = false, updatable = false, length = 20)
    private String stockCode;

    // 주식 이름
    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;

    // 주식 타입
    @Enumerated(EnumType.STRING)
    @Column(name = "stock_type", nullable = false, length = 20)
    private StockType stockType;

    // 마지막으로 동기화한 시간
    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    /**
     * JPA 기본 생성자
     *
     * <p>
     * Hibernate가 DB 조회 결과로 Entity를 생성할 때 사용한다.
     * 외부에서 직접 호출하지 못하도록 protected로 제한한다.
     * </p>
     */
    protected Stock() {
    }

    /**
     * 종목명·시장·동기화 시각을 갱신한다.
     * 종목코드는 변경하지 않는다.
     *
     * <p> 반환값은 종목명 또는 시장이 변경되었는지 나타낸다. </p>
     */
    public boolean refreshMetadata(
            String name,
            StockType type,
            Instant syncedAt
    ) {
        String normalizedName = validateText(
                name,
                100,
                "종목명"
        );

        if (type == null) {
            throw new IllegalArgumentException(
                    "시장 구분이 필요합니다."
            );
        }

        if (syncedAt == null) {
            throw new IllegalArgumentException(
                    "동기화 시각이 필요합니다."
            );
        }

        boolean changed = !Objects.equals(stockName, normalizedName)
                || stockType != type;

        this.stockName = normalizedName;
        this.stockType = type;
        this.lastSyncedAt = syncedAt.truncatedTo(ChronoUnit.SECONDS);

        return changed;
    }

    /**
     * DB 저장 전에 필수 값과 DATETIME 정밀도를 확인한다.
     */
    @PrePersist
    @PreUpdate
    private void validateBeforeSave() {
        this.stockCode = validateText(
                stockCode,
                20,
                "종목코드"
        );

        this.stockName = validateText(
                stockName,
                100,
                "종목명"
        );

        if (stockType == null) {
            throw new IllegalArgumentException(
                    "시장 구분이 필요합니다."
            );
        }

        if (lastSyncedAt != null) {
            lastSyncedAt = lastSyncedAt.truncatedTo(
                    ChronoUnit.SECONDS
            );
        }
    }

    private static String validateText(
            String value,
            int maxLength,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + "은 필수입니다."
            );
        }

        String normalized = value.trim();

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + "은 " + maxLength + "자 이내여야 합니다."
            );
        }

        return normalized;
    }
}