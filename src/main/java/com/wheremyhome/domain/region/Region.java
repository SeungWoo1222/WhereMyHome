package com.wheremyhome.domain.region;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "regions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sido_name", nullable = false, length = 20)
    private String sidoName;

    @Column(name = "sigungu_name", nullable = false, length = 20)
    private String sigunguName;

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
