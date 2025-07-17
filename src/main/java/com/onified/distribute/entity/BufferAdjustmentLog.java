package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "buffer_adjustment_log")
@CompoundIndex(name = "bufferId_adjustmentDate", def = "{'bufferId': 1, 'adjustmentDate': -1}")
@CompoundIndex(name = "productId_locationId_adjustmentDate", def = "{'productId': 1, 'locationId': 1, 'adjustmentDate': -1}")
@CompoundIndex(name = "approvalStatus_adjustmentDate", def = "{'approvalStatus': 1, 'adjustmentDate': -1}")
public class BufferAdjustmentLog {
    @Id
    private String id;
    private String logId;
    private String bufferId;
    private String productId;
    private String locationId;

    private Integer proposedBufferUnits;
    private Integer safetyBufferUnits;
    private Integer finalBufferUnits;
    private Integer consecutiveDaysInZone;
    private Double baseADC;
    private Boolean systemRecommended;
    private Boolean requiresApproval;
    private String approvalStatus;
    private String approvedBy;

    @Indexed
    private LocalDateTime adjustmentDate;
    private String createdBy;


    private String adjustmentType;
    private String triggerReason;
    private String overriddenAction;
    private Integer oldBufferUnits;
    private Integer newBufferUnits;
    private Double changePercentage;
    private LocalDateTime approvalDate;

}
