package com.onified.distribute.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BulkLeadTimeRequestDTO {

    @NotEmpty(message = "Product IDs cannot be empty")
    private List<String> productIds;

    @NotEmpty(message = "Location IDs cannot be empty")
    private List<String> locationIds;
}
