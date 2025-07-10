package com.onified.distribute.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "buffer.threshold.cycle")
public class BufferThresholdCycleConfig {

    /**
     * Default red threshold percentage
     */
    private Double defaultRedThresholdPct = 20.0;

    /**
     * Default yellow threshold percentage
     */
    private Double defaultYellowThresholdPct = 30.0;

    /**
     * Default review period in days
     */
    private Integer defaultReviewPeriodDays = 30;

    /**
     * Maximum allowed threshold percentage
     */
    private Double maxThresholdPct = 100.0;

    /**
     * Minimum allowed threshold percentage
     */
    private Double minThresholdPct = 0.0;

    /**
     * Enable automatic buffer adjustment logging
     */
    private Boolean enableAdjustmentLogging = true;

    /**
     * Maximum number of adjustment logs to keep per buffer
     */
    private Integer maxAdjustmentLogsPerBuffer = 100;

    /**
     * Enable automatic review cycle processing
     */
    private Boolean enableAutomaticReviewProcessing = true;

    /**
     * Review processing batch size
     */
    private Integer reviewProcessingBatchSize = 50;
}
