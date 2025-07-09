package com.onified.distribute.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SeasonalityMatrixResponseDTO {
    private String location;
    private String type;
    private String typeName;
    private Map<String, Double> seasonalityFactors;
}
