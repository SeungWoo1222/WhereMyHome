package com.wheremyhome.api.dto;

public record MonthlyTradeResponse(String month, int avgPrice, int minPrice, int maxPrice, int count) {
}
