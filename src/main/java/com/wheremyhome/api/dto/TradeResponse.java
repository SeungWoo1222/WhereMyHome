package com.wheremyhome.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TradeResponse(LocalDate tradeDate, BigDecimal area, Short floor, Integer price) {
}
