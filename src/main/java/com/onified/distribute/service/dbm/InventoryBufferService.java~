package com.onified.distribute.service.dbm;

import com.onified.distribute.dto.InventoryBufferDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryBufferService {
    InventoryBufferDTO getInventoryBufferById(String bufferId);
    InventoryBufferDTO getInventoryBufferByProductAndLocation(String productId, String locationId);
    Page<InventoryBufferDTO> getAllInventoryBuffers(Pageable pageable);
    Page<InventoryBufferDTO> getInventoryBuffersByProduct(String productId, Pageable pageable);
    Page<InventoryBufferDTO> getInventoryBuffersByLocation(String locationId, Pageable pageable);
    Page<InventoryBufferDTO> getInventoryBuffersByCurrentZone(String currentZone, Pageable pageable);
    Page<InventoryBufferDTO> getActiveInventoryBuffers(Pageable pageable);
    Page<InventoryBufferDTO> getBuffersDueForReview(LocalDateTime currentDate, Pageable pageable);
    InventoryBufferDTO calculateBufferZone(String bufferId);
    InventoryBufferDTO updateBufferInventory(String bufferId, Integer currentInventory, Integer inPipelineQty);
    List<InventoryBufferDTO> getBuffersInZones(List<String> zones);
    void deleteInventoryBuffer(String bufferId);
    Page<InventoryBufferDTO> getInventoryBuffersByProductIds(List<String> productIds, Pageable pageable);
    Page<InventoryBufferDTO> getInventoryBuffersByProductIdsAndCurrentZone(List<String> productIds, String currentZone, Pageable pageable);
    boolean existsByProductAndLocation(String productId, String locationId);

    InventoryBufferDTO createInventoryBuffer(InventoryBufferDTO bufferDto);

    InventoryBufferDTO getBufferByProductAndLocation(String productId, String locationId);

    InventoryBufferDTO updateInventoryBuffer(String bufferId, InventoryBufferDTO bufferDto);

    // Optional: for deactivating buffers when lead time is deactivated
//    void deactivateBufferByProductAndLocation(String productId, String locationId);
}