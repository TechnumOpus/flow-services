package com.onified.distribute.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

@Data
public class BufferCalculationRequestDTO {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Location ID is required")
    private String locationId;

    @NotBlank(message = "Base ADC selection is required")
    @Pattern(regexp = "^(7adc|14adc|30adc)$", message = "Base ADC must be one of: 7adc, 14adc, 30adc")
    private String baseADC;

    @NotNull(message = "Safety factor is required")
    @DecimalMin(value = "0.0", message = "Safety factor must be non-negative")
    private Double safetyFactor;
}
