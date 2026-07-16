package com.wheremyhome.api.dto;

// 월별 집계 응답 — MV에서 조회한 결과
public record MonthlyTradeResponse(String month, int avgPrice, int minPrice, int maxPrice, int count) {
}
