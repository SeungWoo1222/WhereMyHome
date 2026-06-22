package com.wheremyhome.batch;

import com.wheremyhome.domain.apartment.ApartmentComplex;
import com.wheremyhome.domain.pipeline.PipelineLog;
import com.wheremyhome.domain.region.Region;
import com.wheremyhome.infra.molit.MolitApiClient;
import com.wheremyhome.infra.molit.TradeApiResponse;
import com.wheremyhome.repository.ApartmentComplexRepository;
import com.wheremyhome.repository.PipelineLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeCollectService {

    private static final String JOB_NAME = "tradeCollect";

    private final ApartmentComplexRepository complexRepository;
    private final PipelineLogRepository pipelineLogRepository;
    private final MolitApiClient molitApiClient;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRegion(Region region, int year, int month) {
        String sigunguCode = region.getSigunguCode();

        if (pipelineLogRepository.existsBySigunguCodeAndTradeYearAndTradeMonthAndStatus(
                sigunguCode, (short) year, (short) month, "SUCCESS")) {
            return;
        }

        PipelineLog pipelineLog = pipelineLogRepository.save(PipelineLog.start(JOB_NAME, sigunguCode, year, month));

        try {
            List<TradeApiResponse.Item> items = molitApiClient.fetchTrades(sigunguCode, year, month);

            Map<String, ApartmentComplex> complexMap = buildComplexMap(region);

            for (TradeApiResponse.Item item : items) {
                findOrCreateComplex(complexMap, region, item);
            }

            complexMap.values().stream()
                    .filter(c -> c.getId() == null)
                    .forEach(complexRepository::save);

            List<InsertRow> rows = new ArrayList<>();
            for (TradeApiResponse.Item item : items) {
                ApartmentComplex complex = findOrCreateComplex(complexMap, region, item);
                InsertRow row = toInsertRow(complex, item);
                if (row != null) {
                    rows.add(row);
                }
            }

            if (!rows.isEmpty()) {
                insertTradeRecords(rows);
            }

            pipelineLog.success(rows.size());
            pipelineLogRepository.save(pipelineLog);

        } catch (Exception e) {
            log.error("Failed: {} {}/{} - {}", sigunguCode, year, month, e.getMessage());
            pipelineLog.fail(e.getMessage());
            pipelineLogRepository.save(pipelineLog);
        }
    }

    private void insertTradeRecords(List<InsertRow> rows) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO trade_records (id, complex_id, trade_date, area, floor, price, created_at) " +
                "VALUES (nextval('trade_records_id_seq'), ?, ?, ?, ?, ?, NOW())",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        InsertRow r = rows.get(i);
                        ps.setLong(1, r.complexId());
                        ps.setDate(2, Date.valueOf(r.tradeDate()));
                        ps.setBigDecimal(3, r.area());
                        if (r.floor() != null) ps.setShort(4, r.floor());
                        else ps.setNull(4, Types.SMALLINT);
                        ps.setInt(5, r.price());
                    }

                    @Override
                    public int getBatchSize() {
                        return rows.size();
                    }
                }
        );
    }

    private Map<String, ApartmentComplex> buildComplexMap(Region region) {
        Map<String, ApartmentComplex> map = new HashMap<>();
        for (ApartmentComplex c : complexRepository.findByRegion(region)) {
            map.put(complexKey(c.getComplexName(), c.getDongName()), c);
        }
        return map;
    }

    private ApartmentComplex findOrCreateComplex(Map<String, ApartmentComplex> map,
                                                  Region region,
                                                  TradeApiResponse.Item item) {
        String key = complexKey(item.getApartmentName(), item.getDongName());
        return map.computeIfAbsent(key, k -> {
            Short builtYear = parseShort(item.getBuiltYear());
            return ApartmentComplex.of(region, item.getApartmentName(), item.getDongName(), builtYear);
        });
    }

    private InsertRow toInsertRow(ApartmentComplex complex, TradeApiResponse.Item item) {
        try {
            LocalDate tradeDate = LocalDate.of(
                    Integer.parseInt(item.getYear().trim()),
                    Integer.parseInt(item.getMonth().trim()),
                    Integer.parseInt(item.getDay().trim())
            );
            BigDecimal area = new BigDecimal(item.getArea().trim());
            Short floor = parseShort(item.getFloor());
            int price = Integer.parseInt(item.getPrice().replace(",", "").trim());

            return new InsertRow(complex.getId(), tradeDate, area, floor, price);
        } catch (Exception e) {
            log.warn("Skip invalid item: {} - {}", item.getApartmentName(), e.getMessage());
            return null;
        }
    }

    private String complexKey(String name, String dong) {
        return (name == null ? "" : name.trim()) + "|" + (dong == null ? "" : dong.trim());
    }

    private Short parseShort(String value) {
        try {
            return value == null ? null : Short.parseShort(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record InsertRow(Long complexId, LocalDate tradeDate, BigDecimal area, Short floor, int price) {}
}
