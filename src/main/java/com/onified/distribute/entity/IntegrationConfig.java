package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "integration_config")
@CompoundIndex(name = "systemName_integrationType", def = "{'systemName': 1, 'integrationType': 1}")
@CompoundIndex(name = "syncStatus_lastSyncAt", def = "{'syncStatus': 1, 'lastSyncAt': -1}")
@CompoundIndex(name = "isActive_syncFrequency", def = "{'isActive': 1, 'syncFrequency': 1}")
public class IntegrationConfig {
    @Id
    private String id;
    @Indexed(unique = true)
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
    @Indexed
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
