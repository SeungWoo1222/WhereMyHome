package com.wheremyhome.batch;

import com.wheremyhome.batch.chunk.*;
import com.wheremyhome.domain.region.Region;
import com.wheremyhome.infra.molit.MolitApiClient;
import com.wheremyhome.repository.ApartmentComplexRepository;
import com.wheremyhome.repository.PipelineLogRepository;
import com.wheremyhome.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Spring Batch Job/Step 설정.
 *
 * 현재 두 가지 방식 공존:
 *   1. tradeCollectJob: 기존 Tasklet 방식 (range 배치용, 유지)
 *   2. tradeChunkJob:   새 Chunk 방식 (단일 월 처리용)
 *
 * Chunk 방식의 구조:
 *   Reader(API 호출 → 1건씩) → Processor(변환/검증) → Writer(배치 INSERT)
 *   500건 단위 트랜잭션, Skip/Retry 자동 처리.
 */
@Configuration
@RequiredArgsConstructor
public class TradeCollectJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RegionRepository regionRepository;
    private final ApartmentComplexRepository complexRepository;
    private final PipelineLogRepository pipelineLogRepository;
    private final MolitApiClient molitApiClient;
    private final JdbcTemplate jdbcTemplate;

    // === 기존 Tasklet 방식 (Chunk로 대체, 사용 안 함 — 추후 삭제 예정) ===
    // 관련 클래스: TradeCollectTasklet, TradeCollectService
    //
    // @Bean
    // public Job tradeCollectJob() {
    //     return new JobBuilder("tradeCollectJob", jobRepository)
    //             .start(tradeCollectStep())
    //             .build();
    // }
    //
    // @Bean
    // public Step tradeCollectStep() {
    //     return new StepBuilder("tradeCollectStep", jobRepository)
    //             .tasklet(tradeCollectTasklet, transactionManager)
    //             .build();
    // }

    // === 새 Chunk 방식 ===

    /**
     * Chunk 기반 Job.
     * 특정 연월의 전체 시군구 거래 데이터를 수집·저장.
     */
    @Bean
    public Job tradeChunkJob() {
        return new JobBuilder("tradeChunkJob", jobRepository)
                .start(tradeChunkStep(2025, 1))
                .build();
    }

    /**
     * Chunk Step 생성.
     *
     * <TradeItem, TradeInsertRow>chunk(500):
     *   - TradeItem: Reader가 반환하는 타입
     *   - TradeInsertRow: Writer가 받는 타입
     *   - 500: chunk 크기 (500건 모아서 한번에 COMMIT)
     *
     * faultTolerant(): Skip/Retry 활성화
     * skip(Exception.class): 모든 예외에 대해 해당 건만 스킵
     * skipLimit(100000): 최대 10만 건 스킵 허용
     * retry(DataAccessException.class): DB 관련 에러 발생 시 자동 재시도
     * retryLimit(3): 최대 3번 재시도
     */
    public Step tradeChunkStep(int year, int month) {
        List<Region> regions = regionRepository.findAll();

        return new StepBuilder("tradeChunkStep", jobRepository)
                .<TradeItem, TradeInsertRow>chunk(500, transactionManager)
                .reader(new TradeApiItemReader(molitApiClient, pipelineLogRepository, regions, year, month))
                .processor(new TradeItemProcessor(complexRepository))
                .writer(new TradeItemWriter(jdbcTemplate))
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100000)
                .retry(DataAccessException.class)
                .retryLimit(3)
                .build();
    }
}
