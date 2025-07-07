
package com.onified.distribute.service;

import com.onified.distribute.dto.InventoryBufferDTO;

public interface BufferCalculationService {
    InventoryBufferDTO calculateAndCreateBuffer(String productId, String locationId, Double safetyFactor);
    InventoryBufferDTO initializeBuffer(String productId, String locationId, Double safetyFactor);
}