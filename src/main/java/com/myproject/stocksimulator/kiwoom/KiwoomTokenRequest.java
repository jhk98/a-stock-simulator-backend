package com.myproject.stocksimulator.kiwoom;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KiwoomTokenRequest(
        @JsonProperty("grant_type") String grantType,
        @JsonProperty("appkey") String appKey,
        @JsonProperty("secretkey") String secretKey
) {
        @Override
        public String toString() {
                // record 기본 toString()은 모든 필드를 출력하므로 재정의합니다.
                return "KiwoomTokenRequest[credentials=REDACTED]";
        }
}