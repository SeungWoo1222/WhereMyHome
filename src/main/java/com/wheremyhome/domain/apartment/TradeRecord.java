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
@IdClass(TradeRecordId.class) // id + trade_date 복합 PK 선언
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자, protected로 외부 직접 생성 차단
public class TradeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB AUTO_INCREMENT에 id 생성 위임
    private Long id;

    // 거래일자, 파티션 키로 복합 PK에 포함
    @Id
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @ManyToOne(fetch = FetchType.LAZY) // 이 거래는 하나의 아파트 단지에 속함, 접근 시점에 DB 조회 (지연 로딩)
    @JoinColumn(name = "complex_id", nullable = false)
    private ApartmentComplex complex;

    // 전용면적 (㎡), 소수점 있어서 BigDecimal
    @Column(name = "area", nullable = false, precision = 6, scale = 2)
    private BigDecimal area;

    // 층수
    @Column(name = "floor")
    private Short floor;

    // 거래금액 (만원 단위)
    @Column(name = "price", nullable = false)
    private Integer price;

    // 등록 시각, INSERT 시 자동 세팅
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist // INSERT 직전 JPA가 자동 호출
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
