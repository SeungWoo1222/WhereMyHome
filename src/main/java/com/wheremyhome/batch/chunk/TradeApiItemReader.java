package com.wheremyhome.batch.chunk;

import com.wheremyhome.domain.region.Region;
import com.wheremyhome.infra.molit.MolitApiClient;
import com.wheremyhome.infra.molit.TradeApiResponse;
import com.wheremyhome.repository.PipelineLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.util.Iterator;
import java.util.List;

/**
 * 국토부 API에서 거래 데이터를 읽어오는 ItemReader.
 *
 * 동작 방식:
 *   1. regions(시군구 목록)을 순회하면서
 *   2. 각 시군구에 대해 API 호출 → 거래 목록 수신
 *   3. 수신한 목록에서 1건씩 TradeItem으로 반환
 *   4. 한 시군구 목록이 끝나면 다음 시군구로 이동
 *   5. 모든 시군구 완료 → null 반환 → 읽기 종료
 *
 * Spring Batch가 이 read()를 chunk 크기만큼 반복 호출.
 */
@Slf4j
public class TradeApiItemReader implements ItemReader<TradeItem> {

    private final MolitApiClient apiClient;
    private final PipelineLogRepository pipelineLogRepository;
    private final List<Region> regions;
    private final int year;
    private final int month;

    private Iterator<Region> regionIterator;
    private Region currentRegion;
    private Iterator<TradeApiResponse.Item> itemIterator;
    private int regionsDone = 0;

    public TradeApiItemReader(MolitApiClient apiClient,
                              PipelineLogRepository pipelineLogRepository,
                              List<Region> regions,
                              int year, int month) {
        this.apiClient = apiClient;
        this.pipelineLogRepository = pipelineLogRepository;
        this.regions = regions;
        this.year = year;
        this.month = month;
        this.regionIterator = regions.iterator();
    }

    /**
     * Spring Batch가 반복 호출하는 핵심 메서드.
     * null 반환 시 읽기 종료.
     */
    @Override
    public TradeItem read() {
        while (true) {
            // 현재 시군구의 아이템이 남아있으면 1건 반환
            if (itemIterator != null && itemIterator.hasNext()) {
                return new TradeItem(currentRegion, itemIterator.next());
            }

            // 다음 시군구로 이동
            if (!moveToNextRegion()) {
                return null;  // 모든 시군구 완료
            }
        }
    }

    /**
     * 다음 시군구로 이동하면서 API 호출.
     * 이미 성공한 시군구는 skip.
     */
    private boolean moveToNextRegion() {
        while (regionIterator.hasNext()) {
            currentRegion = regionIterator.next();
            String code = currentRegion.getSigunguCode();

            // 이미 성공한 건 skip
            if (pipelineLogRepository.existsBySigunguCodeAndTradeYearAndTradeMonthAndStatus(
                    code, (short) year, (short) month, "SUCCESS")) {
                continue;
            }

            // API 호출
            try {
                List<TradeApiResponse.Item> items = apiClient.fetchTrades(code, year, month);
                itemIterator = items.iterator();
                regionsDone++;

                if (regionsDone % 50 == 0) {
                    log.info("  {}-{}: {}/{} regions done", year, String.format("%02d", month), regionsDone, regions.size());
                }
                return true;
            } catch (Exception e) {
                log.error("API call failed: {} {}/{} - {}", code, year, month, e.getMessage());
                continue;
            }
        }
        return false;
    }
}
