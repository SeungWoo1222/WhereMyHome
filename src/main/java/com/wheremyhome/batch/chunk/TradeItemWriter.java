package com.wheremyhome.batch.chunk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * chunk 단위로 모인 TradeInsertRow를 DB에 배치 INSERT.
 *
 * ON CONFLICT DO NOTHING:
 *   UNIQUE 제약(complex_id, trade_date, area, floor, price)에 걸리면
 *   에러 대신 해당 행만 무시 → 중복 데이터 자동 방지.
 *
 * Spring Batch가 chunk 크기(500건)만큼 모아서 write()를 1번 호출.
 * write() 완료 후 자동 COMMIT.
 */
@Slf4j
public class TradeItemWriter implements ItemWriter<TradeInsertRow> {

    private final JdbcTemplate jdbcTemplate;

    public TradeItemWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void write(Chunk<? extends TradeInsertRow> chunk) {
        List<? extends TradeInsertRow> items = chunk.getItems();
        if (items.isEmpty()) return;

        jdbcTemplate.batchUpdate(
                "INSERT INTO trade_records (id, complex_id, trade_date, area, floor, price, created_at) " +
                "VALUES (nextval('trade_records_id_seq'), ?, ?, ?, ?, ?, NOW()) " +
                "ON CONFLICT (complex_id, trade_date, area, floor, price) DO NOTHING",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TradeInsertRow r = items.get(i);
                        ps.setLong(1, r.complexId());
                        ps.setDate(2, Date.valueOf(r.tradeDate()));
                        ps.setBigDecimal(3, r.area());
                        if (r.floor() != null) ps.setShort(4, r.floor());
                        else ps.setNull(4, Types.SMALLINT);
                        ps.setInt(5, r.price());
                    }

                    @Override
                    public int getBatchSize() {
                        return items.size();
                    }
                }
        );

        log.debug("Written {} records", items.size());
    }
}
