package com.wheremyhome.infra.molit;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MolitApiClient {

    private static final int PAGE_SIZE = 1000;

    private final WebClient webClient;
    private final XmlMapper xmlMapper = new XmlMapper();

    @Value("${api.public-data.apartment-key}")
    private String apiKey;

    public List<TradeApiResponse.Item> fetchTrades(String sigunguCode, int year, int month) {
        String dealYmd = String.format("%d%02d", year, month);
        List<TradeApiResponse.Item> allItems = new ArrayList<>();
        int pageNo = 1;

        while (true) {
            int currentPage = pageNo;
            String xml = fetchWithRetry(sigunguCode, dealYmd, currentPage);

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

    private String fetchWithRetry(String sigunguCode, String dealYmd, int pageNo) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .scheme("https")
                                .host("apis.data.go.kr")
                                .path("/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev")
                                .queryParam("serviceKey", apiKey)
                                .queryParam("LAWD_CD", sigunguCode)
                                .queryParam("DEAL_YMD", dealYmd)
                                .queryParam("numOfRows", PAGE_SIZE)
                                .queryParam("pageNo", pageNo)
                                .build())
                        .header("User-Agent", "Mozilla/5.0")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("429") && attempt < maxRetries) {
                    log.warn("429 rate limited, retry {}/{} after 5s: {}", attempt, maxRetries, sigunguCode);
                    try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                } else {
                    throw e;
                }
            }
        }
        throw new RuntimeException("Max retries exceeded");
    }
}
