package com.wheremyhome.api;

import com.wheremyhome.api.dto.ApartmentResponse;
import com.wheremyhome.api.dto.MonthlyTradeResponse;
import com.wheremyhome.api.dto.TradeResponse;
import com.wheremyhome.repository.ApartmentComplexRepository;
import com.wheremyhome.repository.TradeRecordQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentComplexRepository complexRepository;
    private final TradeRecordQueryRepository tradeRecordQueryRepository;

    @GetMapping
    public Slice<ApartmentResponse> search(@RequestParam(required = false) Long regionId,
                                           @RequestParam(required = false, defaultValue = "") String name,
                                           Pageable pageable) {
        if (regionId != null) {
            return complexRepository.findByRegionIdAndComplexNameContaining(regionId, name, pageable)
                    .map(ApartmentResponse::from);
        }
        return complexRepository.findByComplexNameContaining(name, pageable)
                .map(ApartmentResponse::from);
    }

    @GetMapping("/{id}")
    public ApartmentResponse getOne(@PathVariable Long id) {
        return complexRepository.findById(id)
                .map(ApartmentResponse::from)
                .orElseThrow(() -> new RuntimeException("Apartment not found: " + id));
    }

    @GetMapping("/{id}/trades")
    public List<TradeResponse> getTrades(@PathVariable Long id) {
        return tradeRecordQueryRepository.findByComplexId(id);
    }

    @GetMapping("/{id}/trades/monthly")
    public List<MonthlyTradeResponse> getMonthlyTrades(@PathVariable Long id) {
        return tradeRecordQueryRepository.findMonthlyByComplexId(id);
    }
}
