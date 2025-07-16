package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "replenishment_queue")
@CompoundIndex(name = "queueDate_status", def = "{'queueDate': -1, 'status': 1}")
@CompoundIndex(name = "priorityScore_status", def = "{'priorityScore': -1, 'status': 1}")
@CompoundIndex(name = "locationId_status_priorityScore", def = "{'locationId': 1, 'status': 1, 'priorityScore': -1}")
@CompoundIndex(name = "bufferZone_status", def = "{'bufferZone': 1, 'status': 1}")
public class ReplenishmentQueue {
    @Id
    private String id;
    private String queueId;
    private String productId;
    private String locationId;
    private Integer inHand;
    private Integer inPipelineQty;
    private Integer allocatedQty;
    private Integer netAvailableQty;
    private Integer bufferUnits;
    private Integer bufferGap;
    @Indexed
    private String bufferZone;
    private Double daysOfSupply;
    private Integer recommendedQty;
    private String recommendedAction;
    @Indexed
    private Double priorityScore;
    private Double adcUsed;
    private Double leadTimeDays;
    private List<String> reasonCodes;
    @Indexed
    private LocalDateTime queueDate;
    private String processedBy;
    private LocalDateTime processedAt;
    private String actionTaken;
    private String orderId;
    private String status;
    private Boolean isActive;
}
