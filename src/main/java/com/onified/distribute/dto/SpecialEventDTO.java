package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SpecialEventDTO {
    private String id;
    private String eventId;
    private String eventName;
    private String eventDescription;
    private String productId;
    private String locationId;
    private String category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double changeFactor;
    private String impactType;
    private String eventType;
    private String recurrence;
    private Boolean isActive;
    private String approvalStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String approvedBy;
}
