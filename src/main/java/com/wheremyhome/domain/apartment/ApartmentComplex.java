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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApartmentComplex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "complex_name", nullable = false, length = 100)
    private String complexName;

    @Column(name = "dong_name", length = 50)
    private String dongName;

    @Column(name = "built_year")
    private Short builtYear;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
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
