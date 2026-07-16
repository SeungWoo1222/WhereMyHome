package com.wheremyhome.repository;

import com.wheremyhome.domain.apartment.ApartmentComplex;
import com.wheremyhome.domain.region.Region;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ApartmentComplexRepository extends JpaRepository<ApartmentComplex, Long> {

    List<ApartmentComplex> findByRegion(Region region);

    @EntityGraph(attributePaths = {"region"})
    Slice<ApartmentComplex> findByRegionIdAndComplexNameContaining(Long regionId, String name, Pageable pageable);

    @EntityGraph(attributePaths = {"region"})
    Slice<ApartmentComplex> findByComplexNameContaining(String name, Pageable pageable);
}
