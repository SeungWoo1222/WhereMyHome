package com.wheremyhome.batch;

import com.wheremyhome.repository.PipelineLogRepository;
import com.wheremyhome.repository.RegionRepository;
import com.wheremyhome.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;

/**
 * 매월 전월 실거래가 데이터를 자동 수집하는 스케줄러.
 *
 * 국토부 실거래 신고 기한이 거래 후 30일 이내라서,
 * 매월 1일 시점에는 전월 거래 데이터가 대부분 신고 완료된 상태.
 *
 * Chunk Job을 사용하므로 UNIQUE 제약 + ON CONFLICT DO NOTHING +
 * RateLimiter + Skip/Retry가 전부 적용된 상태로 실행됨.
 * PipelineLog가 지역별 SUCCESS를 기록하므로, 같은 (year, month)로
 * 다시 실행해도 이미 성공한 지역은 건너뛰고 나머지만 재시도한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradeCollectScheduler {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofMinutes(5);

    private final BatchService batchService;
    private final RegionRepository regionRepository;
    private final PipelineLogRepository pipelineLogRepository;
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
        long totalRegions = regionRepository.count();

        log.info("Scheduled collection started: {}-{} ({} regions)", year, String.format("%02d", month), totalRegions);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                log.info("Attempt {}/{}: {}-{}", attempt, MAX_ATTEMPTS, year, String.format("%02d", month));
                batchService.runTradeCollect(year, month, "scheduledChunkJob");
            } catch (Exception e) {
                log.error("Attempt {} failed: {}-{} - {}", attempt, year, String.format("%02d", month), e.getMessage(), e);
            }

            long successCount = pipelineLogRepository.countSuccessRegions((short) year, (short) month);
            if (successCount >= totalRegions) {
                log.info("Scheduled collection completed on attempt {}: {}-{} ({}/{} regions)",
                        attempt, year, String.format("%02d", month), successCount, totalRegions);
                break;
            }
            log.warn("Incomplete after attempt {}: {}/{} regions succeeded", attempt, successCount, totalRegions);

            if (attempt < MAX_ATTEMPTS) {
                sleep(RETRY_DELAY);
            }
        }

        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY monthly_trade_stats");
        log.info("Materialized view refreshed");
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
