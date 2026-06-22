package com.wheremyhome.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class TradeCollectJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TradeCollectTasklet tradeCollectTasklet;

    @Bean
    public Job tradeCollectJob() {
        return new JobBuilder("tradeCollectJob", jobRepository)
                .start(tradeCollectStep())
                .build();
    }

    @Bean
    public Step tradeCollectStep() {
        return new StepBuilder("tradeCollectStep", jobRepository)
                .tasklet(tradeCollectTasklet, transactionManager)
                .build();
    }
}
