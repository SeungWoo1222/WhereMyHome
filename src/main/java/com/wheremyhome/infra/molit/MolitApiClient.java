package com.wheremyhome.infra.molit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MolitApiClient {

    private static final String BASE_URL =
            "https://apis.data.go.kr/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev";

    private final WebClient webClient;

    @Value("${api.public-data.apartment-key}")
    private String apiKey;

    public List<TradeApiResponse.Item> fetchTrades(String sigunguCode, int year, int month) {
        String dealYmd = String.format("%d%02d", year, month);

        TradeApiResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("apis.data.go.kr")
                        .path("/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev")
                        .queryParam("serviceKey", apiKey)
                        .queryParam("LAWD_CD", sigunguCode)
                        .queryParam("DEAL_YMD", dealYmd)
                        .queryParam("numOfRows", 1000)
                        .queryParam("pageNo", 1)
                        .build())
                .retrieve()
                .bodyToMono(TradeApiResponse.class)
                .block();

        if (response == null || response.getBody() == null || response.getBody().getItems() == null) {
            log.warn("Empty response: sigungu={}, year={}, month={}", sigunguCode, year, month);
            return List.of();
        }

        List<TradeApiResponse.Item> items = response.getBody().getItems().getItem();
        return items != null ? items : List.of();
    }
}
