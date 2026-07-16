package com.wheremyhome.batch.chunk;

import com.wheremyhome.domain.apartment.ApartmentComplex;
import com.wheremyhome.domain.region.Region;
import com.wheremyhome.infra.molit.TradeApiResponse;
import com.wheremyhome.repository.ApartmentComplexRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * API 원본 데이터(TradeItem)를 DB INSERT용(TradeInsertRow)으로 변환.
 *
 * 역할:
 *   1. 아파트 단지 찾기/생성 (이름+동으로 식별)
 *   2. 가격 파싱 ("530,000" → 530000)
 *   3. 날짜 변환 (연/월/일 → LocalDate)
 *   4. 유효성 검증 (파싱 실패 시 null 반환 → Spring Batch가 스킵)
 */
@Slf4j
public class TradeItemProcessor implements ItemProcessor<TradeItem, TradeInsertRow> {

    private final ApartmentComplexRepository complexRepository;

    // 지역별 단지 캐시: regionId → (아파트명|동 → ApartmentComplex)
    private final Map<Long, Map<String, ApartmentComplex>> complexCache = new HashMap<>();

    public TradeItemProcessor(ApartmentComplexRepository complexRepository) {
        this.complexRepository = complexRepository;
    }

    /**
     * Spring Batch가 Reader에서 받은 TradeItem마다 호출.
     * null 반환 → 해당 건 스킵 (skipCount에 +1)
     */
    @Override
    public TradeInsertRow process(TradeItem tradeItem) {
        try {
            Region region = tradeItem.region();
            TradeApiResponse.Item item = tradeItem.item();

            // 아파트 단지 찾기/생성
            ApartmentComplex complex = findOrCreateComplex(region, item);

            // 데이터 변환
            LocalDate tradeDate = LocalDate.of(
                    Integer.parseInt(item.getYear().trim()),
                    Integer.parseInt(item.getMonth().trim()),
                    Integer.parseInt(item.getDay().trim())
            );
            BigDecimal area = new BigDecimal(item.getArea().trim());
            Short floor = parseShort(item.getFloor());
            int price = Integer.parseInt(item.getPrice().replace(",", "").trim());

            return new TradeInsertRow(complex.getId(), tradeDate, area, floor, price);
        } catch (Exception e) {
            log.warn("Skip invalid item: {} - {}", tradeItem.item().getApartmentName(), e.getMessage());
            return null;
        }
    }

    private ApartmentComplex findOrCreateComplex(Region region, TradeApiResponse.Item item) {
        Map<String, ApartmentComplex> regionMap = complexCache.computeIfAbsent(
                region.getId(),
                id -> {
                    Map<String, ApartmentComplex> map = new HashMap<>();
                    for (ApartmentComplex c : complexRepository.findByRegion(region)) {
                        map.put(complexKey(c.getComplexName(), c.getDongName()), c);
                    }
                    return map;
                }
        );

        String key = complexKey(item.getApartmentName(), item.getDongName());
        return regionMap.computeIfAbsent(key, k -> {
            Short builtYear = parseShort(item.getBuiltYear());
            ApartmentComplex newComplex = ApartmentComplex.of(region, item.getApartmentName(), item.getDongName(), builtYear);
            return complexRepository.save(newComplex);
        });
    }

    private String complexKey(String name, String dong) {
        return (name == null ? "" : name.trim()) + "|" + (dong == null ? "" : dong.trim());
    }

    private Short parseShort(String value) {
        try {
            return value == null ? null : Short.parseShort(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
