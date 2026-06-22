package com.wheremyhome.repository;

import com.wheremyhome.domain.pipeline.PipelineLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineLogRepository extends JpaRepository<PipelineLog, Long> {

    boolean existsBySigunguCodeAndTradeYearAndTradeMonthAndStatus(
            String sigunguCode, short tradeYear, short tradeMonth, String status);
}
