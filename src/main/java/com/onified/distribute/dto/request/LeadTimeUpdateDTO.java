package com.onified.distribute.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Data
public class LeadTimeUpdateDTO {
    private String supplierName;

    @Min(value = 1, message = "MOQ must be greater than 0")
    private Integer moq;

    @Min(value = 0, message = "Order lead time cannot be negative")
    private Double orderLeadTime;

    @Min(value = 0, message = "Manufacturing time cannot be negative")
    private Double manufacturingTime;

    @Min(value = 0, message = "Transport time cannot be negative")
    private Double transportTime;

    @NotBlank(message = "Updated by is required")
    private String updatedBy;
}
