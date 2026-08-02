package com.wheremyhome.repository;

import com.wheremyhome.domain.pipeline.PipelineLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PipelineLogRepository extends JpaRepository<PipelineLog, Long> {

    boolean existsBySigunguCodeAndTradeYearAndTradeMonthAndStatus(
            String sigunguCode, short tradeYear, short tradeMonth, String status);

    // 해당 연월에 SUCCESS로 기록된 서로 다른 시군구 수 (전체 지역 수와 비교해 완료 여부 판단용)
    @Query("SELECT COUNT(DISTINCT p.sigunguCode) FROM PipelineLog p " +
            "WHERE p.tradeYear = :year AND p.tradeMonth = :month AND p.status = 'SUCCESS'")
    long countSuccessRegions(@Param("year") short year, @Param("month") short month);
}
