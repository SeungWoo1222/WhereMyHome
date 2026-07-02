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

/**
 * 실거래가 수집의 실제 비즈니스 로직을 담당하는 Service.
 *
 * Tasklet에서 분리한 이유:
 *   @Transactional은 Spring 프록시 기반이라, 같은 클래스 내부에서 호출하면 작동 안 함.
 *   별도 @Service로 분리해야 프록시가 정상적으로 트랜잭션을 걸어줌.
 *
 * 처리 흐름:
 *   1. pipeline_logs에서 이미 성공한 건인지 확인 → 중복 실행 방지
 *   2. 국토부 API 호출 → 거래 데이터 수신
 *   3. 아파트 단지 찾기 또는 신규 생성
 *   4. 거래 기록 DB 저장 (JdbcTemplate 배치 INSERT)
 *   5. pipeline_logs에 성공/실패 기록
 */
// 사용 안 함 — TradeCollectTasklet 전용이었음. Tasklet과 함께 추후 삭제 예정.
@Slf4j
// @Service
@RequiredArgsConstructor
public class TradeCollectService {

    private static final String JOB_NAME = "tradeCollect";

    private final ApartmentComplexRepository complexRepository;
    private final PipelineLogRepository pipelineLogRepository;
    private final MolitApiClient molitApiClient;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 하나의 시군구 + 연월 조합에 대해 실거래가 데이터를 수집·저장.
     *
     * @Transactional(propagation = REQUIRES_NEW):
     *   기존 트랜잭션(Tasklet의 Step 트랜잭션)과 별개로 새 트랜잭션을 시작.
     *   → 이 메서드 안에서 커밋/롤백이 독립적으로 일어남.
     *   → 한 시군구가 실패해도 다른 시군구 데이터는 이미 커밋되어 안전.
     *
     *   만약 REQUIRES_NEW가 없으면:
     *   Step 전체가 하나의 트랜잭션 → 20년치 다 끝나야 커밋 → 중간에 죽으면 전부 롤백.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRegion(Region region, int year, int month) {
        String sigunguCode = region.getSigunguCode();

        // 멱등성 보장: 이미 성공한 시군구+연월 조합은 skip
        if (pipelineLogRepository.existsBySigunguCodeAndTradeYearAndTradeMonthAndStatus(
                sigunguCode, (short) year, (short) month, "SUCCESS")) {
            return;
        }

        // 실행 로그 생성 (RUNNING 상태)
        PipelineLog pipelineLog = pipelineLogRepository.save(PipelineLog.start(JOB_NAME, sigunguCode, year, month));

        try {
            // 1단계: 국토부 API 호출 → 거래 데이터 수신
            List<TradeApiResponse.Item> items = molitApiClient.fetchTrades(sigunguCode, year, month);

            // 2단계: 해당 지역의 기존 아파트 단지를 Map으로 로드
            //        key = "아파트명|법정동", value = ApartmentComplex 엔티티
            Map<String, ApartmentComplex> complexMap = buildComplexMap(region);

            // 3단계: API 응답을 순회하며 신규 단지 발견 시 Map에 추가 (아직 DB 미저장)
            for (TradeApiResponse.Item item : items) {
                findOrCreateComplex(complexMap, region, item);
            }

            // 4단계: 신규 단지를 DB에 저장 (ID 채번됨)
            //        getId() == null인 것만 필터링 → 기존 단지는 이미 ID 있음
            complexMap.values().stream()
                    .filter(c -> c.getId() == null)
                    .forEach(complexRepository::save);

            // 5단계: API 응답 → InsertRow 변환 (단지 ID가 필요하므로 4단계 이후에 실행)
            List<InsertRow> rows = new ArrayList<>();
            for (TradeApiResponse.Item item : items) {
                ApartmentComplex complex = findOrCreateComplex(complexMap, region, item);
                InsertRow row = toInsertRow(complex, item);
                if (row != null) {
                    rows.add(row);
                }
            }

            // 6단계: 거래 기록 배치 INSERT
            if (!rows.isEmpty()) {
                insertTradeRecords(rows);
            }

            // 성공 기록
            pipelineLog.success(rows.size());
            pipelineLogRepository.save(pipelineLog);

        } catch (Exception e) {
            log.error("Failed: {} {}/{} - {}", sigunguCode, year, month, e.getMessage());
            // 실패 기록 (에러 메시지 포함)
            pipelineLog.fail(e.getMessage());
            pipelineLogRepository.save(pipelineLog);
        }
    }

    /**
     * JdbcTemplate.batchUpdate로 거래 기록을 한번에 INSERT.
     *
     * JPA(saveAll) 대신 JdbcTemplate을 쓰는 이유:
     *   trade_records는 파티션 테이블 + 복합 PK (id, trade_date).
     *   Hibernate가 복합 PK에 @GeneratedValue를 지원 안 함.
     *   → JdbcTemplate으로 직접 SQL 실행, nextval()로 시퀀스에서 ID 채번.
     *
     * BatchPreparedStatementSetter:
     *   setValues()가 각 행마다 호출되어 PreparedStatement에 값을 세팅.
     *   getBatchSize()만큼 반복 후 한번에 DB로 전송 → 네트워크 왕복 최소화.
     */
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

    /**
     * 해당 지역의 기존 아파트 단지를 Map으로 로드.
     * key = "아파트명|법정동" 조합으로 단지를 식별.
     * → 같은 이름이라도 동이 다르면 별개 단지로 취급.
     */
    private Map<String, ApartmentComplex> buildComplexMap(Region region) {
        Map<String, ApartmentComplex> map = new HashMap<>();
        for (ApartmentComplex c : complexRepository.findByRegion(region)) {
            map.put(complexKey(c.getComplexName(), c.getDongName()), c);
        }
        return map;
    }

    /**
     * Map에서 단지를 찾고, 없으면 새로 생성해서 Map에 추가.
     * computeIfAbsent: key가 없을 때만 람다를 실행해서 값을 넣음.
     * → 같은 아파트가 여러 번 나와도 1번만 생성됨.
     */
    private ApartmentComplex findOrCreateComplex(Map<String, ApartmentComplex> map,
                                                  Region region,
                                                  TradeApiResponse.Item item) {
        String key = complexKey(item.getApartmentName(), item.getDongName());
        return map.computeIfAbsent(key, k -> {
            Short builtYear = parseShort(item.getBuiltYear());
            return ApartmentComplex.of(region, item.getApartmentName(), item.getDongName(), builtYear);
        });
    }

    /**
     * API 응답 Item → DB INSERT용 InsertRow로 변환.
     * 가격의 콤마 제거, 연/월/일 → LocalDate 변환 등 데이터 정제.
     * 변환 실패 시 null 반환 → 해당 건은 스킵됨.
     */
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

    /**
     * DB INSERT용 데이터 레코드.
     * Java 16+ record: 불변 객체를 간결하게 정의. (getter, equals, hashCode 자동 생성)
     */
    private record InsertRow(Long complexId, LocalDate tradeDate, BigDecimal area, Short floor, int price) {}
}
