package com.wheremyhome.api;

import com.wheremyhome.api.dto.RegionResponse;
import com.wheremyhome.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    // GET /api/regions
    // GET /api/regions?sido=경기도
    // 전국 시군구 목록, Caffeine 캐시 적용 → 첫 요청만 DB 조회
    // 반환: { "경기도": [...], "서울특별시": [...] } 시도별로 그룹핑된 Map
    @GetMapping
    public Map<String, List<RegionResponse>> getAll(@RequestParam(required = false) String sido) {
        return regionService.getAll(sido);
    }
}
