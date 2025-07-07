package com.onified.distribute.service;

import com.onified.distribute.dto.InventoryOrderPipelineDTO;
import com.onified.distribute.dto.ReplenishmentQueueDTO;
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

}