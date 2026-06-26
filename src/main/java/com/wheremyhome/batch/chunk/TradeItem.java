package com.wheremyhome.batch.chunk;

import com.wheremyhome.domain.region.Region;
import com.wheremyhome.infra.molit.TradeApiResponse;

/**
 * Reader → Processor로 전달되는 데이터.
 * API 응답 Item에 "어느 지역에서 온 건지" 정보를 붙인 것.
 *
 * Processor가 아파트 단지를 찾으려면 region 정보가 필요하기 때문.
 */
public record TradeItem(
        Region region,
        TradeApiResponse.Item item
) {}
