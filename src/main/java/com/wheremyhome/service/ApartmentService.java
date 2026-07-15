package com.wheremyhome.service;

import com.wheremyhome.api.dto.ApartmentResponse;
import com.wheremyhome.api.dto.MonthlyTradeResponse;
import com.wheremyhome.api.dto.TradeResponse;
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

    // regionId 있으면 지역+이름 검색, 없으면 이름만 검색. Slice로 반환 → COUNT 쿼리 없음
    public Slice<ApartmentResponse> search(Long regionId, String name, Pageable pageable) {
        if (regionId != null) {
            return complexRepository.findByRegionIdAndComplexNameContaining(regionId, name, pageable)
                    .map(ApartmentResponse::from);
        }
        return complexRepository.findByComplexNameContaining(name, pageable)
                .map(ApartmentResponse::from);
    }

    // id로 단지 하나 조회, 없으면 예외
    public ApartmentResponse getOne(Long id) {
        return complexRepository.findById(id)
                .map(ApartmentResponse::from)
                .orElseThrow(() -> new RuntimeException("Apartment not found: " + id));
    }

    // 최근 1년 거래 목록 (날짜·면적·층·가격)
    public List<TradeResponse> getTrades(Long id) {
        return tradeRecordQueryRepository.findByComplexId(id);
    }

    // 최근 3년 월별 집계 (MV에서 조회) → 차트 데이터
    public List<MonthlyTradeResponse> getMonthlyTrades(Long id) {
        return tradeRecordQueryRepository.findMonthlyByComplexId(id);
    }
}
