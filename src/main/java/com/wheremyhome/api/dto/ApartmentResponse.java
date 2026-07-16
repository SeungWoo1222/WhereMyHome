package com.wheremyhome.api.dto;

import com.wheremyhome.domain.apartment.ApartmentComplex;

// 아파트 검색/조회 응답 — 엔티티에서 필요한 필드만 추려서 반환
public record ApartmentResponse(Long id, String complexName, String dongName, Short builtYear,
                                 String sidoName, String sigunguName) {

    public static ApartmentResponse from(ApartmentComplex complex) {
        return new ApartmentResponse(
                complex.getId(),
                complex.getComplexName(),
                complex.getDongName(),
                complex.getBuiltYear(),
                complex.getRegion().getSidoName(),
                complex.getRegion().getSigunguName()
        );
    }
}
