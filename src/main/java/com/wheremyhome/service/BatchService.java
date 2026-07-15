package com.wheremyhome.service;

import com.wheremyhome.batch.TradeCollectJobConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final JobLauncher jobLauncher;
    private final JobRepository jobRepository;
    private final TradeCollectJobConfig jobConfig;

    // 특정 연월 거래 데이터를 Chunk Job으로 조립해서 실행
    // jobNamePrefix로 실행 주체 구분 (수동: tradeChunkJob, 스케줄러: scheduledChunkJob)
    public void runTradeCollect(int year, int month, String jobNamePrefix) throws Exception {
        Step chunkStep = jobConfig.tradeChunkStep(year, month);

        Job chunkJob = new JobBuilder(jobNamePrefix + "-" + year + "-" + month, jobRepository)
                .start(chunkStep)
                .build();

        JobParameters params = new JobParametersBuilder()
                .addLong("year", (long) year)
                .addLong("month", (long) month)
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(chunkJob, params);
    }
}
