package com.myproject.stocksimulator.kiwoom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KiwoomStockListResponse(

        @JsonProperty("list")
        List<KiwoomStockInfo> stocks,

        @JsonProperty("return_code")
        Integer returnCode,

        @JsonProperty("return_msg")
        String returnMsg

) {
}
