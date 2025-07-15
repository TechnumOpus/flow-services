package com.onified.distribute.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
public class BufferAdjustmentRequestDTO {
    @NotBlank(message = "Buffer ID is required")
    private String bufferId;

    @NotNull(message = "Recommended buffer units is required")
    @Min(value = 0, message = "Recommended buffer units must be non-negative")
    private Integer recommendedBufferUnits;

    private String adjustmentReason;
    private String comments;
    private Boolean overrideSystemRecommendation;

    @NotBlank(message = "Updated by is required")
    private String updatedBy;
}
