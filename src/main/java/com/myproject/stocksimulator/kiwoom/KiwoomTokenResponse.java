package com.myproject.stocksimulator.kiwoom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KiwoomTokenResponse(
        @JsonProperty("token") String token,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_dt") String expiresDt,
        @JsonProperty("return_code") Integer returnCode,
        @JsonProperty("return_msg") String returnMsg
) {
        @Override
        public String toString() {
                return "KiwoomTokenResponse[token=REDACTED, returnCode="
                        + returnCode + "]";
        }
}