package com.onified.distribute.service.order;

import com.onified.distribute.dto.InventoryOrderPipelineDTO;
import com.onified.distribute.dto.ReplenishmentQueueDTO;
import com.onified.distribute.dto.ReplenishmentQueueFilterDTO;
import com.onified.distribute.dto.ReplenishmentQueueResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReplenishmentQueueService {
    void processReplenishmentQueue();
    void generateDailyReplenishmentQueue();
    ReplenishmentQueueDTO createQueueItem(ReplenishmentQueueDTO queueDTO);
    ReplenishmentQueueDTO updateQueueItem(String queueId, ReplenishmentQueueDTO queueDTO);
    ReplenishmentQueueDTO getQueueItemById(String queueId);
    Page<ReplenishmentQueueDTO> getQueueItemsByStatus(String status, Pageable pageable);
    Page<ReplenishmentQueueDTO> getQueueItemsByBufferZoneAndStatus(String bufferZone, String status, Pageable pageable);
    Page<InventoryOrderPipelineDTO> getInTransitOrders(String locationId, Pageable pageable);
    void cancelOrder(String orderId);
    Page<ReplenishmentQueueDTO> getAllReplenishmentQueues(Pageable pageable);
    /**
     * Get all replenishment queues with enhanced response format
     */
    Page<ReplenishmentQueueResponseDTO> getAllReplenishmentQueuesEnhanced(Pageable pageable);

    /**
     * Get replenishment queues with filters and enhanced response format
     */
    Page<ReplenishmentQueueResponseDTO> getReplenishmentQueuesWithFilters(
            ReplenishmentQueueFilterDTO filters,
            Pageable pageable
    );

}