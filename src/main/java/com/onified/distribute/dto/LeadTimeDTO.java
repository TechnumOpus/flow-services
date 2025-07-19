package com.onified.distribute.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeadTimeDTO {
    private String id;
    private String productId;
    private String locationId;
    private String supplierId;
    @PositiveOrZero(message = "order lead time must be non-negative")
    private Double orderLeadTime;
    @PositiveOrZero(message = "manufacturing  time must be non-negative")
    private Double manufacturingTime;
    @PositiveOrZero(message = "Transport time must be non-negative")
    private Double transportTime;
    @NotNull(message = "MOQ cannot be null")
    @PositiveOrZero(message = "MOQ must be non-negative")
    private Integer moq;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;
}