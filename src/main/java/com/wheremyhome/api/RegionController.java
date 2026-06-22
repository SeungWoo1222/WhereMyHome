package com.wheremyhome.api;

import com.wheremyhome.api.dto.RegionResponse;
import com.wheremyhome.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionRepository regionRepository;

    @GetMapping
    public Map<String, List<RegionResponse>> getAll(@RequestParam(required = false) String sido) {
        var regions = regionRepository.findAll().stream()
                .filter(r -> sido == null || r.getSidoName().contains(sido))
                .map(RegionResponse::from)
                .collect(Collectors.groupingBy(RegionResponse::sidoName));
        return regions;
    }
}
