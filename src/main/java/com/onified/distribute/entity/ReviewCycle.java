package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "review_cycles")
@CompoundIndex(name = "isActive_nextStartDate", def = "{'isActive': 1, 'nextStartDate': 1}")
@CompoundIndex(name = "frequency_isActive", def = "{'frequency': 1, 'isActive': 1}")
public class ReviewCycle {
    @Id
    private String id;
    @Indexed(unique = true)
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
    @Indexed
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
