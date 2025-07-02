package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "lead_times")
@CompoundIndex(name = "productId_locationId_isActive", def = "{'productId': 1, 'locationId': 1, 'isActive': 1}")
@CompoundIndex(name = "locationId_isActive", def = "{'locationId': 1, 'isActive': 1}")
@CompoundIndex(name = "effectiveFrom_effectiveTo", def = "{'effectiveFrom': 1, 'effectiveTo': 1}")
public class LeadTime {
    @Id
    private String id;
    private String productId;
    private String locationId;
    private String supplierId;
    private Double orderLeadTime;
    private Double manufacturingTime;
    private Double transportTime;
    private Double bufferLeadTimeDays;
    private Double leadTimeVariability;
    private Double onTimeDeliveryPct;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    @Indexed
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
