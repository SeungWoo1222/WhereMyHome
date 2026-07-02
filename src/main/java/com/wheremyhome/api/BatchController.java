package com.wheremyhome.api;

import com.wheremyhome.batch.TradeCollectJobConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 배치 Job을 REST API로 실행하는 컨트롤러.
 *
 * 현재 사용 엔드포인트:
 *   /trade-collect/chunk — Chunk 방식, 단일 월
 *
 * 사용 안 함 (Tasklet 방식, 추후 삭제 예정):
 *   /trade-collect, /trade-collect/range
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    // private final Job tradeCollectJob; // Tasklet 전용, JobConfig에서 빈 주석 처리됨
    private final JobRepository jobRepository;
    private final TradeCollectJobConfig jobConfig;

    // === 기존 Tasklet 방식 (사용 안 함 — 추후 삭제 예정) ===
    //
    // /**
    //  * [Tasklet] 특정 연월 1개 수집.
    //  */
    // @PostMapping("/trade-collect")
    // public String run(@RequestParam int year, @RequestParam int month) throws Exception {
    //     JobParameters params = new JobParametersBuilder()
    //             .addLong("year", (long) year)
    //             .addLong("month", (long) month)
    //             .addLong("runAt", System.currentTimeMillis())
    //             .toJobParameters();
    //
    //     jobLauncher.run(tradeCollectJob, params);
    //     return "Job started: year=" + year + ", month=" + month;
    // }
    //
    // /**
    //  * [Tasklet] 연도 범위 수집.
    //  */
    // @PostMapping("/trade-collect/range")
    // public String runRange(@RequestParam int startYear,
    //                        @RequestParam int endYear,
    //                        @RequestParam(defaultValue = "1") int startMonth,
    //                        @RequestParam(defaultValue = "12") int endMonth) throws Exception {
    //     JobParameters params = new JobParametersBuilder()
    //             .addLong("startYear", (long) startYear)
    //             .addLong("endYear", (long) endYear)
    //             .addLong("startMonth", (long) startMonth)
    //             .addLong("endMonth", (long) endMonth)
    //             .addLong("runAt", System.currentTimeMillis())
    //             .toJobParameters();
    //
    //     jobLauncher.run(tradeCollectJob, params);
    //     return "Job started: " + startYear + "-" + startMonth + " ~ " + endYear + "-" + endMonth;
    // }

    /**
     * [Chunk] 특정 연월 1개를 Chunk 방식으로 수집.
     *
     * - 500건 단위 트랜잭션 (실패 시 500건만 롤백)
     * - Skip/Retry 자동 처리
     * - INSERT ON CONFLICT DO NOTHING (중복 자동 무시)
     * - RateLimiter로 API 호출 속도 제한
     */
    @PostMapping("/trade-collect/chunk")
    public String runChunk(@RequestParam int year, @RequestParam int month) throws Exception {
        Step chunkStep = jobConfig.tradeChunkStep(year, month);

        Job chunkJob = new JobBuilder("tradeChunkJob-" + year + "-" + month, jobRepository)
                .start(chunkStep)
                .build();

        JobParameters params = new JobParametersBuilder()
                .addLong("year", (long) year)
                .addLong("month", (long) month)
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(chunkJob, params);
        return "Chunk job started: year=" + year + ", month=" + month;
    }
}
