package com.wheremyhome.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
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

    private final JobLauncher jobLauncher;
    private final JobRepository jobRepository;
    private final TradeCollectJobConfig jobConfig;

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
            Step chunkStep = jobConfig.tradeChunkStep(year, month);
            Job chunkJob = new JobBuilder("scheduledChunkJob-" + year + "-" + month, jobRepository)
                    .start(chunkStep)
                    .build();

            JobParameters params = new JobParametersBuilder()
                    .addLong("year", (long) year)
                    .addLong("month", (long) month)
                    .addLong("runAt", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(chunkJob, params);
            log.info("Scheduled collection completed: {}-{}", year, String.format("%02d", month));
        } catch (Exception e) {
            log.error("Scheduled collection failed: {}-{} - {}", year, String.format("%02d", month), e.getMessage(), e);
        }
    }
}
