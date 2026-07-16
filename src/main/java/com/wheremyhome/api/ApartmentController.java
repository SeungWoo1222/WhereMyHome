package com.wheremyhome.api;

import com.wheremyhome.api.dto.ApartmentResponse;
import com.wheremyhome.api.dto.MonthlyTradeResponse;
import com.wheremyhome.api.dto.TradeResponse;
import com.wheremyhome.service.ApartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentService apartmentService;

    // GET /api/apartments?name=힐스테이트&page=0&size=100
    @GetMapping
    public Slice<ApartmentResponse> search(@RequestParam(required = false) Long regionId,
                                           @RequestParam(required = false, defaultValue = "") String name,
                                           Pageable pageable) {
        return apartmentService.search(regionId, name, pageable);
    }

    // GET /api/apartments/id
    @GetMapping("/{id}")
    public ApartmentResponse getOne(@PathVariable Long id) {
        return apartmentService.getOne(id);
    }

    // GET /api/apartments/id/trades
    @GetMapping("/{id}/trades")
    public List<TradeResponse> getTrades(@PathVariable Long id) {
        return apartmentService.getTrades(id);
    }

    // GET /api/apartments/id/trades/monthly
    @GetMapping("/{id}/trades/monthly")
    public List<MonthlyTradeResponse> getMonthlyTrades(@PathVariable Long id) {
        return apartmentService.getMonthlyTrades(id);
    }
}
