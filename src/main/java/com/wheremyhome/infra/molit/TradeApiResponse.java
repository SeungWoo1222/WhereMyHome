package com.wheremyhome.infra.molit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@JacksonXmlRootElement(localName = "response")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@NoArgsConstructor
public class TradeApiResponse {

    @JacksonXmlProperty(localName = "body")
    private Body body;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {

        @JacksonXmlProperty(localName = "items")
        private Items items;

        @JacksonXmlProperty(localName = "totalCount")
        private int totalCount;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        private List<Item> item;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        @JacksonXmlProperty(localName = "aptNm")
        private String apartmentName;

        @JacksonXmlProperty(localName = "umdNm")
        private String dongName;

        @JacksonXmlProperty(localName = "buildYear")
        private String builtYear;

        @JacksonXmlProperty(localName = "excluUseAr")
        private String area;

        @JacksonXmlProperty(localName = "floor")
        private String floor;

        @JacksonXmlProperty(localName = "dealAmount")
        private String price;

        @JacksonXmlProperty(localName = "dealYear")
        private String year;

        @JacksonXmlProperty(localName = "dealMonth")
        private String month;

        @JacksonXmlProperty(localName = "dealDay")
        private String day;
    }
}
