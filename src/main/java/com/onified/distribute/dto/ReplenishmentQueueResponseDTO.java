package com.onified.distribute.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplenishmentQueueResponseDTO {
    private String id;
    private String queueId;
    private String productId;
    private String productName;
    private String locationId;
    private String locationName;
    private Integer bufferUnits;
    private Integer inHand; // currentInventory
    private Integer inPipeline; // inPipelineQty
    private Integer netAvailability; // netAvailableQty
    private Integer bufferGap;
    private Integer moq; // from products collection
    private Double daysOfSupply;
    private String bufferZone;
    private String recommendedAction;
    private Double priorityScore;
    private String status;
    private LocalDateTime queueDate;
    private List<String> reasonCodes;
}