package com.wheremyhome.repository;

import com.wheremyhome.domain.apartment.ApartmentComplex;
import com.wheremyhome.domain.region.Region;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ApartmentComplexRepository extends JpaRepository<ApartmentComplex, Long> {

    List<ApartmentComplex> findByRegion(Region region);

    @EntityGraph(attributePaths = {"region"})
    Slice<ApartmentComplex> findByRegionIdAndComplexNameContaining(Long regionId, String name, Pageable pageable);

    // 단지명 OR 지역명(시도·시군구)으로 검색. regions는 249행이라 인덱스 없이 조인해도 빠름
    @EntityGraph(attributePaths = {"region"})
    @Query("SELECT ac FROM ApartmentComplex ac WHERE ac.complexName LIKE CONCAT('%', :name, '%') " +
            "OR ac.region.sidoName LIKE CONCAT('%', :name, '%') " +
            "OR ac.region.sigunguName LIKE CONCAT('%', :name, '%')")
    Slice<ApartmentComplex> searchByNameOrRegion(@Param("name") String name, Pageable pageable);
}
