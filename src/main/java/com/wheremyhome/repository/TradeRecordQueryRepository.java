package com.wheremyhome.repository;

import com.wheremyhome.api.dto.MonthlyTradeResponse;
import com.wheremyhome.api.dto.TradeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TradeRecordQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    // 전체 거래 이력 (기간 제한 없음). 단일 단지 조회라 파티션 인덱스로 충분히 빠름(~11ms)
    public List<TradeResponse> findByComplexId(Long complexId) {
        return jdbcTemplate.query(
                "SELECT trade_date, area, floor, price FROM trade_records WHERE complex_id = ? ORDER BY trade_date DESC",
                (rs, rowNum) -> new TradeResponse(
                        rs.getDate("trade_date").toLocalDate(),
                        rs.getBigDecimal("area"),
                        rs.getObject("floor") != null ? rs.getShort("floor") : null,
                        rs.getInt("price")
                ),
                complexId
        );
    }

    // 월별 집계 (MV 조회, 최근 3년) — 평소 사용, 0.373ms 수준
    // area가 null이면 전체 면적 합산, 지정하면 그 면적만 (MV는 complex_id+month+area 단위로 이미 나뉘어 있음)
    public List<MonthlyTradeResponse> findMonthlyByComplexId(Long complexId, BigDecimal area) {
        String from = LocalDate.now().minusYears(3).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        if (area != null) {
            return jdbcTemplate.query(
                    "SELECT month, avg_price, min_price, max_price, cnt " +
                    "FROM monthly_trade_stats WHERE complex_id = ? AND month >= ? AND area = ? " +
                    "ORDER BY month",
                    (rs, rowNum) -> new MonthlyTradeResponse(
                            rs.getString("month"),
                            rs.getInt("avg_price"),
                            rs.getInt("min_price"),
                            rs.getInt("max_price"),
                            rs.getInt("cnt")
                    ),
                    complexId, from, area
            );
        }
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

    // 월별 집계 (원본 trade_records 직접 집계, 전체 기간) — "전체" 선택 시에만 사용, ~47ms
    // MV는 최근 3년만 갖고 있어 그 이전 데이터는 원본을 파티션 인덱스로 스캔해 실시간 집계
    public List<MonthlyTradeResponse> findMonthlyAllByComplexId(Long complexId, BigDecimal area) {
        String areaCondition = area != null ? "AND area = ? " : "";
        Object[] params = area != null ? new Object[]{complexId, area} : new Object[]{complexId};
        return jdbcTemplate.query(
                "SELECT TO_CHAR(DATE_TRUNC('month', trade_date), 'YYYY-MM') AS month, " +
                "ROUND(AVG(price)) AS avg_price, MIN(price) AS min_price, MAX(price) AS max_price, COUNT(*) AS cnt " +
                "FROM trade_records WHERE complex_id = ? " + areaCondition +
                "GROUP BY DATE_TRUNC('month', trade_date) ORDER BY month",
                (rs, rowNum) -> new MonthlyTradeResponse(
                        rs.getString("month"),
                        rs.getInt("avg_price"),
                        rs.getInt("min_price"),
                        rs.getInt("max_price"),
                        rs.getInt("cnt")
                ),
                params
        );
    }
}
