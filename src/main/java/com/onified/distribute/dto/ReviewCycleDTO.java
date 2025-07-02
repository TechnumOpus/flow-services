package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewCycleDTO {
    private String id;
    private String cycleId;
    private String cycleName;
    private String description;
    private Integer startDay;
    private Integer endDay;
    private String frequency;
    private LocalDateTime nextStartDate;
    private LocalDateTime nextEndDate;
    private Boolean autoCalculateNext;
    private String timezone;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
