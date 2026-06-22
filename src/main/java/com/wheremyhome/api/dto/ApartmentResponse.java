package com.wheremyhome.api.dto;

import com.wheremyhome.domain.apartment.ApartmentComplex;

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
