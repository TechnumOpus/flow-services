package com.onified.distribute.service.order;

import com.onified.distribute.dto.InventoryOrderPipelineDTO;
import com.onified.distribute.dto.ReplenishmentQueueDTO;
import com.onified.distribute.dto.ReplenishmentQueueFilterDTO;
import com.onified.distribute.dto.ReplenishmentQueueResponseDTO;
import com.onified.distribute.entity.ReplenishmentQueue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReplenishmentQueueService {
    Page<ReplenishmentQueueResponseDTO> getAllReplenishmentQueuesEnhanced(Pageable pageable, String queueId);
    Page<ReplenishmentQueueResponseDTO> getReplenishmentQueuesWithFilters(ReplenishmentQueueFilterDTO filters, Pageable pageable);
    Optional<ReplenishmentQueue> getQueueEntityById(String queueId);

    void processReplenishmentQueue();
    void generateDailyReplenishmentQueue();
    Page<ReplenishmentQueueDTO> getAllReplenishmentQueues(Pageable pageable);
    ReplenishmentQueueDTO createQueueItem(ReplenishmentQueueDTO queueDTO);
    ReplenishmentQueueDTO updateQueueItem(String queueId, ReplenishmentQueueDTO queueDTO);
    ReplenishmentQueueDTO getQueueItemById(String queueId);
    Page<ReplenishmentQueueDTO> getQueueItemsByStatus(String status, Pageable pageable);
    Page<ReplenishmentQueueDTO> getQueueItemsByBufferZoneAndStatus(String bufferZone, String status, Pageable pageable);
    Page<InventoryOrderPipelineDTO> getInTransitOrders(String locationId, Pageable pageable);
    void cancelOrder(String orderId);
    Page<ReplenishmentQueueResponseDTO> getAllReplenishmentQueuesEnhanced(Pageable pageable);
    Optional<ReplenishmentQueue> getQueueItemByProductIdAndLocationId(String productId, String locationId);
    ReplenishmentQueueDTO convertToDto(ReplenishmentQueue queueItem); // Expose for controller use
}
