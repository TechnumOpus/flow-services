package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class IntegrationConfigDTO {
    private String id;
    private String configId;
    private String systemName;
    private String integrationType;
    private String productId;
    private String locationId;
    private String category;
    private String endpointUrl;
    private String authenticationType;
    private String syncFrequency;
    private Integer batchSize;
    private LocalDateTime lastSyncAt;
    private LocalDateTime lastSuccessfulSync;
    private LocalDateTime lastErrorAt;
    private String lastErrorMessage;
    private String syncStatus;
    private Integer consecutiveFailures;
    private Map<String, String> fieldMappings;
    private List<String> transformationRules;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
