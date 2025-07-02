package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DailyConsumptionLogDTO {
    private String id;
    private String logId;
    private String productId;
    private String locationId;
    private LocalDateTime consumptionDate;
    private Integer quantityConsumed;
    private String transactionType;
    private String transactionRef;
    private String sourceSystem;
    private LocalDateTime createdAt;
    private String createdBy;
}
