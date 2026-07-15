package com.wheremyhome.api;

import com.wheremyhome.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 배치 Job을 REST API로 실행하는 컨트롤러.
 *
 * 현재 사용 엔드포인트:
 *   /trade-collect/chunk — Chunk 방식, 단일 월
 *
 * 사용 안 함 (Tasklet 방식, 추후 삭제 예정):
 *   /trade-collect, /trade-collect/range
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    /**
     * [Chunk] 특정 연월 1개를 Chunk 방식으로 수집.
     *
     * - 500건 단위 트랜잭션 (실패 시 500건만 롤백)
     * - Skip/Retry 자동 처리
     * - INSERT ON CONFLICT DO NOTHING (중복 자동 무시)
     * - RateLimiter로 API 호출 속도 제한
     */

    // POST /api/batch/trade-collect/chunk?year=2026&month=7
    // 국토부 API에서 특정 연월 거래 데이터 수집
    // 500건 단위 트랜잭션, 중복 자동 무시
    // 스케줄러가 자동 실행하지만 수동으로도 호출 가능
    @PostMapping("/trade-collect/chunk")
    public String runChunk(@RequestParam int year, @RequestParam int month) throws Exception {
        batchService.runTradeCollect(year, month, "tradeChunkJob");
        return "Chunk job started: year=" + year + ", month=" + month;
    }
}
