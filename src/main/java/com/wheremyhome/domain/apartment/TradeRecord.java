package com.wheremyhome.domain.apartment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_records")
@IdClass(TradeRecordId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Id
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complex_id", nullable = false)
    private ApartmentComplex complex;

    @Column(name = "area", nullable = false, precision = 6, scale = 2)
    private BigDecimal area;  // 전용면적 (㎡)

    @Column(name = "floor")
    private Short floor;

    @Column(name = "price", nullable = false)
    private Integer price;  // 거래금액 (만원)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static TradeRecord of(ApartmentComplex complex, LocalDate tradeDate, BigDecimal area, Short floor, Integer price) {
        TradeRecord t = new TradeRecord();
        t.complex = complex;
        t.tradeDate = tradeDate;
        t.area = area;
        t.floor = floor;
        t.price = price;
        return t;
    }
}
