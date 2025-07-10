package com.onified.distribute.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "buffer.calculation")
public class BufferCalculationConfig {

    /**
     * Default safety factor percentage if not provided
     */
    private Double defaultSafetyFactor = 20.0;

    /**
     * Default base ADC selection if not provided
     */
    private String defaultBaseADC = "14adc";

    /**
     * Maximum safety factor allowed
     */
    private Double maxSafetyFactor = 100.0;

    /**
     * Minimum safety factor allowed
     */
    private Double minSafetyFactor = 0.0;

    /**
     * Enable/disable calculation caching
     */
    private Boolean enableCaching = false;

    /**
     * Cache TTL in minutes
     */
    private Integer cacheTtlMinutes = 30;
}
