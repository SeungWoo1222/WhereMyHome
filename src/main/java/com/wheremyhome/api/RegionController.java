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

    @GetMapping
    public Map<String, List<RegionResponse>> getAll(@RequestParam(required = false) String sido) {
        return regionService.getAll(sido);
    }
}
