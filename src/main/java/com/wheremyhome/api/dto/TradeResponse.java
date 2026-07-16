package com.wheremyhome.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// 거래 이력 응답 — JDBC로 직접 매핑
public record TradeResponse(LocalDate tradeDate, BigDecimal area, Short floor, Integer price) {
}
