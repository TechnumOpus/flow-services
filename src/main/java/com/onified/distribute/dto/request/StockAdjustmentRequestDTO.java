package com.onified.distribute.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;

@Data
public class StockAdjustmentRequestDTO {
    @NotBlank(message = "Queue ID cannot be blank")
    private String queueId;
    private String productId;
    private String locationId;
    @NotNull(message = "Revised in-hand quantity cannot be null")
    @Min(value = 0, message = "Revised in-hand quantity cannot be negative")
    private Integer revisedInHand;
    private List<String> reasonCodes;
}