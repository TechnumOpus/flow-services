package com.onified.distribute.service;

import com.onified.distribute.dto.BufferCalculationRequestDTO;
import com.onified.distribute.dto.BufferCalculationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BufferCalculationService {

    /**
     * Calculate buffer quantity for a specific product and location
     */
    BufferCalculationResponseDTO calculateBufferQuantity(BufferCalculationRequestDTO request);

    /**
     * Get buffer calculations for multiple products at a location
     */
    List<BufferCalculationResponseDTO> calculateBufferQuantitiesForLocation(String locationId,
                                                                            String baseADC,
                                                                            Double safetyFactor,
                                                                            Pageable pageable);

    /**
     * Get buffer calculations for a product across multiple locations
     */
    List<BufferCalculationResponseDTO> calculateBufferQuantitiesForProduct(String productId,
                                                                           String baseADC,
                                                                           Double safetyFactor,
                                                                           Pageable pageable);

    /**
     * Validate if calculation is possible for given product and location
     */
    boolean canCalculateBuffer(String productId, String locationId);
}
