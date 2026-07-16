package com.wheremyhome.global.exception;

// 존재하지 않는 아파트 단지를 조회했을 때 (→ HTTP 404)
public class ApartmentNotFoundException extends RuntimeException {

    public ApartmentNotFoundException(Long id) {
        super("Apartment not found: " + id);
    }
}
