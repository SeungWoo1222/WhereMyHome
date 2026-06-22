package com.wheremyhome.repository;

import com.wheremyhome.domain.apartment.ApartmentComplex;
import com.wheremyhome.domain.region.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ApartmentComplexRepository extends JpaRepository<ApartmentComplex, Long> {

    List<ApartmentComplex> findByRegion(Region region);

    Page<ApartmentComplex> findByRegionIdAndComplexNameContaining(Long regionId, String name, Pageable pageable);

    Page<ApartmentComplex> findByComplexNameContaining(String name, Pageable pageable);
}
