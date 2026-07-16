package com.wheremyhome.domain.apartment;

import com.wheremyhome.domain.region.Region;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "apartment_complexes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자, protected로 외부 직접 생성 차단
public class ApartmentComplex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB AUTO_INCREMENT에 id 생성 위임
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 이 단지는 하나의 지역에 속함, 접근 시점에 DB 조회 (지연 로딩)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    // 아파트 단지명 (예: 힐스테이트 광교)
    @Column(name = "complex_name", nullable = false, length = 100)
    private String complexName;

    // 법정동명 (예: 광교동)
    @Column(name = "dong_name", length = 50)
    private String dongName;

    // 준공연도 (예: 2015)
    @Column(name = "built_year")
    private Short builtYear;

    // 최초 등록 시각, INSERT 시 자동 세팅
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist // INSERT 직전 JPA가 자동 호출
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static ApartmentComplex of(Region region, String complexName, String dongName, Short builtYear) {
        ApartmentComplex a = new ApartmentComplex();
        a.region = region;
        a.complexName = complexName;
        a.dongName = dongName;
        a.builtYear = builtYear;
        return a;
    }
}
