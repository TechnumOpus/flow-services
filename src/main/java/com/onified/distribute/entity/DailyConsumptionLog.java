package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;

@Data
@Document(collection = "daily_consumption_log")
@CompoundIndex(name = "productId_locationId_consumptionDate", def = "{'productId': 1, 'locationId': 1, 'consumptionDate': -1}")
@CompoundIndex(name = "locationId_consumptionDate", def = "{'locationId': 1, 'consumptionDate': -1}")
public class DailyConsumptionLog {
    @Id
    private String id;
    private String logId;
    @Indexed
    private String productId;
    @Indexed
    private String locationId;
    @Indexed
    private LocalDateTime consumptionDate;
    private Integer quantityConsumed;
    private String transactionType;
    private String transactionRef;
    private String sourceSystem;
    private LocalDateTime createdAt;
    private String createdBy;

}
