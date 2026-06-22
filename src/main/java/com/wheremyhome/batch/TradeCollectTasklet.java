package com.wheremyhome.batch;

import com.wheremyhome.domain.region.Region;
import com.wheremyhome.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeCollectTasklet implements Tasklet {

    private final RegionRepository regionRepository;
    private final TradeCollectService tradeCollectService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Map<String, Object> params = chunkContext.getStepContext().getJobParameters();
        List<Region> regions = regionRepository.findAll();

        if (params.containsKey("startYear")) {
            int startYear = ((Long) params.get("startYear")).intValue();
            int endYear = ((Long) params.get("endYear")).intValue();
            int startMonth = ((Long) params.get("startMonth")).intValue();
            int endMonth = ((Long) params.get("endMonth")).intValue();

            log.info("Starting range collection: {}-{} ~ {}-{}, regions={}", startYear, startMonth, endYear, endMonth, regions.size());

            for (int year = startYear; year >= endYear; year--) {
                int mStart = (year == startYear) ? startMonth : 12;
                int mEnd = (year == endYear) ? endMonth : 1;
                for (int month = mStart; month >= mEnd; month--) {
                    processMonth(regions, year, month);
                }
            }
        } else {
            int year = ((Long) params.get("year")).intValue();
            int month = ((Long) params.get("month")).intValue();
            processMonth(regions, year, month);
        }

        return RepeatStatus.FINISHED;
    }

    private void processMonth(List<Region> regions, int year, int month) {
        log.info("Processing {}-{}...", year, String.format("%02d", month));
        int done = 0;

        for (Region region : regions) {
            tradeCollectService.processRegion(region, year, month);
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            done++;
            if (done % 50 == 0) {
                log.info("  {}-{}: {}/{} regions done", year, String.format("%02d", month), done, regions.size());
            }
        }

        log.info("Completed {}-{}: {}/{} regions", year, String.format("%02d", month), done, regions.size());
    }
}
