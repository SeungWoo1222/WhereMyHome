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

/**
 * 실거래가 수집 Tasklet.
 *
 * Tasklet 인터페이스를 구현하면 execute() 메서드 하나만 정의하면 됨.
 * Spring Batch가 이 execute()를 호출해서 실제 배치 작업을 수행.
 *
 * 이 Tasklet의 역할:
 * 1. JobParameter에서 연도/월 범위를 꺼냄
 * 2. regions 테이블에서 전국 시군구 목록을 조회
 * 3. 시군구별로 TradeCollectService.processRegion()을 호출
 *
 * 실제 API 호출 + DB 저장 로직은 TradeCollectService에 위임.
 * → @Transactional 프록시가 작동하려면 다른 클래스에서 호출해야 하기 때문.
 *   (같은 클래스 내 호출은 Spring 프록시를 우회해서 트랜잭션이 안 걸림)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradeCollectTasklet implements Tasklet {

    private final RegionRepository regionRepository;
    private final TradeCollectService tradeCollectService;

    /**
     * Spring Batch가 호출하는 핵심 메서드.
     *
     * @param contribution Step에 대한 메타 정보 (읽기/쓰기/스킵 건수 등 기록용)
     * @param chunkContext Step 실행 컨텍스트 (JobParameter 접근 가능)
     * @return FINISHED → Step 종료, CONTINUABLE → 다시 execute() 호출
     *
     * JobParameter 접근:
     *   chunkContext.getStepContext().getJobParameters() → Map<String, Object>
     *   Long 타입으로 저장했으므로 (Long)으로 캐스팅 필요.
     *
     * 두 가지 모드:
     * 1. range 모드: startYear~endYear 범위를 역순으로 순회
     * 2. single 모드: 특정 year/month 하나만 처리
     */
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

            // 최신 → 과거 순서로 처리 (최신 데이터부터 확보)
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

    /**
     * 특정 연월의 전국 시군구를 순회하며 데이터 수집.
     *
     * 249개 시군구를 순차적으로 처리.
     * 각 시군구는 TradeCollectService에서 독립 트랜잭션(REQUIRES_NEW)으로 처리됨.
     * → 하나의 시군구가 실패해도 다른 시군구에 영향 없음.
     *
     * 50개 단위로 진행 상황 로그 출력.
     */
    private void processMonth(List<Region> regions, int year, int month) {
        log.info("Processing {}-{}...", year, String.format("%02d", month));
        int done = 0;

        for (Region region : regions) {
            tradeCollectService.processRegion(region, year, month);
            done++;
            if (done % 50 == 0) {
                log.info("  {}-{}: {}/{} regions done", year, String.format("%02d", month), done, regions.size());
            }
        }

        log.info("Completed {}-{}: {}/{} regions", year, String.format("%02d", month), done, regions.size());
    }
}
