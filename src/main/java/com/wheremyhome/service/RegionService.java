package com.wheremyhome.service;

import com.wheremyhome.api.dto.RegionResponse;
import com.wheremyhome.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;

    @Cacheable(value = "regions", key = "#sido ?: 'all'")
    public Map<String, List<RegionResponse>> getAll(String sido) {
        return regionRepository.findAll().stream()
                .filter(r -> sido == null || r.getSidoName().contains(sido))
                .map(RegionResponse::from)
                .collect(Collectors.groupingBy(RegionResponse::sidoName));
    }
}
