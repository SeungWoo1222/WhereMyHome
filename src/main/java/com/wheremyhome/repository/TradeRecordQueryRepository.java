package com.wheremyhome.repository;

import com.wheremyhome.api.dto.MonthlyTradeResponse;
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

    public List<MonthlyTradeResponse> findMonthlyByComplexId(Long complexId) {
        String from = LocalDate.now().minusYears(3).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        return jdbcTemplate.query(
                "SELECT month, SUM(avg_price * cnt) / SUM(cnt) AS avg_price, " +
                "MIN(min_price) AS min_price, MAX(max_price) AS max_price, SUM(cnt) AS cnt " +
                "FROM monthly_trade_stats WHERE complex_id = ? AND month >= ? " +
                "GROUP BY month ORDER BY month",
                (rs, rowNum) -> new MonthlyTradeResponse(
                        rs.getString("month"),
                        rs.getInt("avg_price"),
                        rs.getInt("min_price"),
                        rs.getInt("max_price"),
                        rs.getInt("cnt")
                ),
                complexId, from
        );
    }
}
