package com.wheremyhome.infra.molit;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 국토부 아파트 실거래가 API 클라이언트.
 *
 * RateLimiter 적용:
 *   초당 5건으로 제한 → 429 에러 방지.
 *   6번째 호출은 다음 초까지 자동 대기.
 *   수동 Thread.sleep + retry 로직을 프레임워크가 대신 처리.
 */
@Slf4j
@Component
public class MolitApiClient {

    private static final int PAGE_SIZE = 1000;

    private final WebClient webClient;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final RateLimiter rateLimiter;

    @Value("${api.public-data.apartment-key}")
    private String apiKey;

    public MolitApiClient(WebClient webClient) {
        this.webClient = webClient;
        this.rateLimiter = RateLimiter.of("molit-api",
                RateLimiterConfig.custom()
                        .limitForPeriod(5)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ofSeconds(30))
                        .build()
        );
    }

    /**
     * 특정 시군구+연월의 거래 데이터를 전부 가져옴.
     * 1000건 초과 시 페이지 넘기면서 전부 수집.
     */
    public List<TradeApiResponse.Item> fetchTrades(String sigunguCode, int year, int month) {
        String dealYmd = String.format("%d%02d", year, month);
        List<TradeApiResponse.Item> allItems = new ArrayList<>();
        int pageNo = 1;

        while (true) {
            int currentPage = pageNo;

            // RateLimiter: 초당 5건 제한, 초과 시 자동 대기
            rateLimiter.acquirePermission();

            String xml = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev")
                            .queryParam("serviceKey", apiKey)
                            .queryParam("LAWD_CD", sigunguCode)
                            .queryParam("DEAL_YMD", dealYmd)
                            .queryParam("numOfRows", PAGE_SIZE)
                            .queryParam("pageNo", currentPage)
                            .build())
                    .header("User-Agent", "Mozilla/5.0")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            try {
                TradeApiResponse response = xmlMapper.readValue(xml, TradeApiResponse.class);
                if (response.getBody() == null || response.getBody().getItems() == null) {
                    break;
                }

                List<TradeApiResponse.Item> items = response.getBody().getItems().getItem();
                if (items == null || items.isEmpty()) {
                    break;
                }

                allItems.addAll(items);

                int totalCount = response.getBody().getTotalCount();
                if (allItems.size() >= totalCount) {
                    break;
                }

                pageNo++;
            } catch (Exception e) {
                log.error("XML parse failed: sigungu={}, year={}, month={}, page={} - {}", sigunguCode, year, month, currentPage, e.getMessage());
                break;
            }
        }

        return allItems;
    }
}
