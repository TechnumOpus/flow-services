package com.onified.distribute.dto.sap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SapResponse {
    @JsonProperty("n0:ZFMSD_STOCK_DETAILResponse")
    private StockDetailResponse stockDetailResponse;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockDetailResponse {
        @JsonProperty("GT_DATA")
        private DataWrapper gtData;

        @JsonProperty("ET_RETURN")
        private ReturnWrapper etReturn;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataWrapper {
        private List<StockItem> item;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReturnWrapper {
        private ReturnItem item;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReturnItem {
        @JsonProperty("TYPE")
        private String type;

        @JsonProperty("ID")
        private String id;

        @JsonProperty("NUMBER")
        private String number;

        @JsonProperty("MESSAGE")
        private String message;

        @JsonProperty("LOG_NO")
        private String logNo;

        @JsonProperty("LOG_MSG_NO")
        private String logMsgNo;

        @JsonProperty("MESSAGE_V1")
        private String messageV1;

        @JsonProperty("MESSAGE_V2")
        private String messageV2;

        @JsonProperty("MESSAGE_V3")
        private String messageV3;

        @JsonProperty("MESSAGE_V4")
        private String messageV4;

        @JsonProperty("PARAMETER")
        private String parameter;

        @JsonProperty("ROW")
        private String row;

        @JsonProperty("FIELD")
        private String field;

        @JsonProperty("SYSTEM")
        private String system;

        // Add any other fields that might appear
        @JsonProperty("CLASS")
        private String clazz;

        @JsonProperty("CLIENT")
        private String client;

        @JsonProperty("CODE")
        private String code;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockItem {
        @JsonProperty("ZDATE")
        private String zdate;

        @JsonProperty("SKU")
        private String sku;

        @JsonProperty("PLANT")
        private String plant;

        @JsonProperty("SKU_DESC")
        private String skuDesc;

        @JsonProperty("CUSTOMER_CODE")
        private String customerCode;

        @JsonProperty("CUSTOMER_NAME")
        private String customerName;

        @JsonProperty("PROGRAM_NAME")
        private String programName;

        @JsonProperty("STOCK")
        private String stock;

        @JsonProperty("SO_INWARD")
        private String soInward;

        @JsonProperty("SO_OUTWARD")
        private String soOutward;
    }
}