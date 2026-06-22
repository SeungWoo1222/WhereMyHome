package com.wheremyhome.api;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job tradeCollectJob;

    @PostMapping("/trade-collect")
    public String run(@RequestParam int year, @RequestParam int month) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("year", (long) year)
                .addLong("month", (long) month)
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(tradeCollectJob, params);
        return "Job started: year=" + year + ", month=" + month;
    }

    @PostMapping("/trade-collect/range")
    public String runRange(@RequestParam int startYear,
                           @RequestParam int endYear,
                           @RequestParam(defaultValue = "1") int startMonth,
                           @RequestParam(defaultValue = "12") int endMonth) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("startYear", (long) startYear)
                .addLong("endYear", (long) endYear)
                .addLong("startMonth", (long) startMonth)
                .addLong("endMonth", (long) endMonth)
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(tradeCollectJob, params);
        return "Job started: " + startYear + "-" + startMonth + " ~ " + endYear + "-" + endMonth;
    }
}
