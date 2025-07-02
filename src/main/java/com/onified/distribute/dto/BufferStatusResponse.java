package com.onified.distribute.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BufferStatusResponse {
    private boolean success;
    private String locationId;
    private int processedCount;
    private int updatedCount;
    private int errorCount;
    private int criticalCount;
    private LocalDateTime timestamp;
    private String message;
}
