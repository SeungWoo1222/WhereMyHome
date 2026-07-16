package com.wheremyhome.api.dto;

import com.wheremyhome.domain.region.Region;

// 지역 목록 응답
public record RegionResponse(Long id, String sidoName, String sigunguName, String sigunguCode) {

    public static RegionResponse from(Region region) {
        return new RegionResponse(region.getId(), region.getSidoName(), region.getSigunguName(), region.getSigunguCode());
    }
}
