package com.wheremyhome.domain.region;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "regions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자, protected로 외부 직접 생성 차단
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB AUTO_INCREMENT에 id 생성 위임
    private Long id;

    // 시도명 (예: 경기도)
    @Column(name = "sido_name", nullable = false, length = 20)
    private String sidoName;

    // 시군구명 (예: 수원시 영통구)
    @Column(name = "sigungu_name", nullable = false, length = 20)
    private String sigunguName;

    // 국토부 API 시군구 코드 (예: 41170)
    @Column(name = "sigungu_code", nullable = false, length = 5)
    private String sigunguCode;

    public static Region of(String sidoName, String sigunguName, String sigunguCode) {
        Region r = new Region();
        r.sidoName = sidoName;
        r.sigunguName = sigunguName;
        r.sigunguCode = sigunguCode;
        return r;
    }
}
