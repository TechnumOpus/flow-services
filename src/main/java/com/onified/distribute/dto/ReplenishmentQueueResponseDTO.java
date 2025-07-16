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
    private Integer inHand;
    private Integer inPipeline;
    private Integer netAvailability;
    private Integer bufferGap;
    private Integer finalQuantity;
    private Integer moq;
    private Double daysOfSupply;
    private String bufferZone;
    private String recommendedAction;
    private Double priorityScore;
    private String status;
    private LocalDateTime queueDate;
    private List<String> reasonCodes;
}