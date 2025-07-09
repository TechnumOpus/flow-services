package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "special_events")
@CompoundIndex(name = "productId_locationId_isActive", def = "{'productId': 1, 'locationId': 1, 'isActive': 1}")
@CompoundIndex(name = "locationId_startDate", def = "{'locationId': 1, 'startDate': 1}")
@CompoundIndex(name = "eventType_startDate", def = "{'eventType': 1, 'startDate': 1}")
public class SpecialEvent {
    @Id
    private String id;
    private String eventId;
    private String eventName;
    private String eventDescription;
    private String productId;
    private String locationId;
    private String category;
    @Indexed
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double changeFactor;
    private String impactType;
    @Indexed
    private String eventType;
    private String recurrence;
    @Indexed
    private Boolean isActive;
    private String approvalStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String approvedBy;

    private List<String> affectedSkus;
}
