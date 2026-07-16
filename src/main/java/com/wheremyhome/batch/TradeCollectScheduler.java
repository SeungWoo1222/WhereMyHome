package com.wheremyhome.batch;

import com.wheremyhome.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 매월 전월 실거래가 데이터를 자동 수집하는 스케줄러.
 *
 * 국토부 실거래 신고 기한이 거래 후 30일 이내라서,
 * 매월 1일 시점에는 전월 거래 데이터가 대부분 신고 완료된 상태.
 *
 * Chunk Job을 사용하므로 UNIQUE 제약 + ON CONFLICT DO NOTHING +
 * RateLimiter + Skip/Retry가 전부 적용된 상태로 실행됨.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradeCollectScheduler {

    private final BatchService batchService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * cron = "0 0 2 1 * *"
     *         초 분 시 일 월 요일
     * → 매월 1일 새벽 2시 실행
     */
    @Scheduled(cron = "0 0 2 1 * *")
    public void collectLastMonth() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int year = lastMonth.getYear();
        int month = lastMonth.getMonthValue();

        log.info("Scheduled collection started: {}-{}", year, String.format("%02d", month));

        try {
            batchService.runTradeCollect(year, month, "scheduledChunkJob");
            log.info("Scheduled collection completed: {}-{}", year, String.format("%02d", month));

            jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY monthly_trade_stats");
            log.info("Materialized view refreshed");
        } catch (Exception e) {
            log.error("Scheduled collection failed: {}-{} - {}", year, String.format("%02d", month), e.getMessage(), e);
        }
    }
}
