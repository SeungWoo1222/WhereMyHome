package com.wheremyhome.service;

import com.wheremyhome.api.dto.ApartmentResponse;
import com.wheremyhome.api.dto.MonthlyTradeResponse;
import com.wheremyhome.api.dto.TradeResponse;
import com.wheremyhome.global.exception.ApartmentNotFoundException;
import com.wheremyhome.repository.ApartmentComplexRepository;
import com.wheremyhome.repository.TradeRecordQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApartmentService {

    private final ApartmentComplexRepository complexRepository;
    private final TradeRecordQueryRepository tradeRecordQueryRepository;

    // regionId 있으면 지역+이름 검색, 없으면 단지명 OR 지역명 검색. Slice로 반환 → COUNT 쿼리 없음
    public Slice<ApartmentResponse> search(Long regionId, String name, Pageable pageable) {
        if (regionId != null) {
            return complexRepository.findByRegionIdAndComplexNameContaining(regionId, name, pageable)
                    .map(ApartmentResponse::from);
        }
        return complexRepository.searchByNameOrRegion(name, pageable)
                .map(ApartmentResponse::from);
    }

    // id로 단지 하나 조회, 없으면 예외
    public ApartmentResponse getOne(Long id) {
        return complexRepository.findById(id)
                .map(ApartmentResponse::from)
                .orElseThrow(() -> new ApartmentNotFoundException(id));
    }

    // 전체 거래 목록 (날짜·면적·층·가격)
    public List<TradeResponse> getTrades(Long id) {
        return tradeRecordQueryRepository.findByComplexId(id);
    }

    // 월별 집계 → 차트 데이터. all=false면 MV(최근 3년, 빠름), all=true면 원본 직접 집계(전체 기간)
    // area가 null이면 전체 면적 합산, 지정하면 그 면적만 필터
    public List<MonthlyTradeResponse> getMonthlyTrades(Long id, boolean all, java.math.BigDecimal area) {
        return all
                ? tradeRecordQueryRepository.findMonthlyAllByComplexId(id, area)
                : tradeRecordQueryRepository.findMonthlyByComplexId(id, area);
    }
}
