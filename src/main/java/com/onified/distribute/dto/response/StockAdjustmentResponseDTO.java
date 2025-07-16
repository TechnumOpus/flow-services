package com.onified.distribute.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class StockAdjustmentResponseDTO {
    private String queueId;
    private String productId;
    private String locationId;
    private Integer bufferUnits;
    private Integer inHand;
    private Integer revisedInHand;
    private List<String> reasonCodes;
}