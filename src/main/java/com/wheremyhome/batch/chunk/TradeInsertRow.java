package com.wheremyhome.batch.chunk;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Processor → Writer로 전달되는 데이터.
 * API 원본에서 파싱·변환이 끝난 DB INSERT용 데이터.
 */
public record TradeInsertRow(
        Long complexId,
        LocalDate tradeDate,
        BigDecimal area,
        Short floor,
        int price
) {}
