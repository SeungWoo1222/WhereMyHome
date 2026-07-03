package com.wheremyhome.repository;

import com.wheremyhome.api.dto.TradeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TradeRecordQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<TradeResponse> findByComplexId(Long complexId) {
        LocalDate from = LocalDate.now().minusYears(1);
        return jdbcTemplate.query(
                "SELECT trade_date, area, floor, price FROM trade_records WHERE complex_id = ? AND trade_date >= ? ORDER BY trade_date DESC",
                (rs, rowNum) -> new TradeResponse(
                        rs.getDate("trade_date").toLocalDate(),
                        rs.getBigDecimal("area"),
                        rs.getObject("floor") != null ? rs.getShort("floor") : null,
                        rs.getInt("price")
                ),
                complexId, java.sql.Date.valueOf(from)
        );
    }
}
