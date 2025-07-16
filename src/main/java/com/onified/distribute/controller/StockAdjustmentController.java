package com.onified.distribute.controller;

import com.onified.distribute.dto.ReplenishmentQueueDTO;
import com.onified.distribute.dto.ReplenishmentQueueResponseDTO;
import com.onified.distribute.dto.request.StockAdjustmentRequestDTO;
import com.onified.distribute.dto.response.StockAdjustmentResponseDTO;
import com.onified.distribute.entity.ReplenishmentQueue;
import com.onified.distribute.exception.ResourceNotFoundException;
import com.onified.distribute.service.order.ReplenishmentQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/stock-adjustments")
@RequiredArgsConstructor
public class StockAdjustmentController {

    private final ReplenishmentQueueService replenishmentQueueService;

    @GetMapping
    public ResponseEntity<Page<StockAdjustmentResponseDTO>> getStockAdjustments(
            Pageable pageable,
            @RequestParam(required = false) String queueId) {
        log.info("Fetching stock adjustments with pageable: {}, queueId: {}", pageable, queueId);
        Page<ReplenishmentQueueResponseDTO> queues = replenishmentQueueService.getAllReplenishmentQueuesEnhanced(pageable, queueId);
        return ResponseEntity.ok(queues.map(this::mapToStockAdjustmentResponse));
    }

    @PutMapping
    public ResponseEntity<StockAdjustmentResponseDTO> updateStockAdjustment(@Valid @RequestBody StockAdjustmentRequestDTO request) {
        log.info("Updating stock adjustment for queueId: {}", request.getQueueId());

        // Get the DTO directly - it already throws ResourceNotFoundException if not found
        ReplenishmentQueueDTO queueDTO = replenishmentQueueService.getQueueItemById(request.getQueueId());

        // Update the DTO with new values
        queueDTO.setInHand(request.getRevisedInHand());
        queueDTO.setReasonCodes(request.getReasonCodes());
        queueDTO.setProcessedAt(LocalDateTime.now());
        queueDTO.setProcessedBy("SYSTEM");
        queueDTO.setActionTaken("ADJUSTED");

        ReplenishmentQueueDTO updatedQueue = replenishmentQueueService.updateQueueItem(queueDTO.getQueueId(), queueDTO);
        ReplenishmentQueueResponseDTO responseDTO = convertToEnhancedDto(updatedQueue);
        return ResponseEntity.ok(mapToStockAdjustmentResponse(responseDTO));
    }

    @DeleteMapping
    public ResponseEntity<Void> revertStockAdjustment(@RequestParam String queueId) {
        log.info("Reverting stock adjustment for queueId: {}", queueId);

        ReplenishmentQueueDTO queueDTO = replenishmentQueueService.getQueueItemById(queueId);

        // Update the DTO with revert values
        queueDTO.setActionTaken(null);
        queueDTO.setStatus("PENDING");
        queueDTO.setProcessedAt(LocalDateTime.now());
        queueDTO.setProcessedBy("SYSTEM");

        replenishmentQueueService.updateQueueItem(queueId, queueDTO);
        return ResponseEntity.noContent().build();
    }

    private StockAdjustmentResponseDTO mapToStockAdjustmentResponse(ReplenishmentQueueResponseDTO queue) {
        StockAdjustmentResponseDTO response = new StockAdjustmentResponseDTO();
        response.setQueueId(queue.getQueueId());
        response.setProductId(queue.getProductId());
        response.setLocationId(queue.getLocationId());
        response.setBufferUnits(queue.getBufferUnits());
        response.setInHand(queue.getInHand());
        response.setRevisedInHand(queue.getInHand());
        response.setReasonCodes(queue.getReasonCodes());
        return response;
    }

    private ReplenishmentQueueResponseDTO convertToEnhancedDto(ReplenishmentQueueDTO queue) {
        ReplenishmentQueueResponseDTO dto = new ReplenishmentQueueResponseDTO();
        dto.setId(queue.getId());
        dto.setQueueId(queue.getQueueId());
        dto.setProductId(queue.getProductId());
        dto.setLocationId(queue.getLocationId());
        dto.setBufferUnits(queue.getBufferUnits());
        dto.setInHand(queue.getInHand());
        dto.setInPipeline(queue.getInPipelineQty());
        dto.setNetAvailability(queue.getNetAvailableQty());
        dto.setBufferGap(queue.getBufferGap());
        dto.setDaysOfSupply(queue.getDaysOfSupply());
        dto.setBufferZone(queue.getBufferZone());
        dto.setRecommendedAction(queue.getRecommendedAction());
        dto.setStatus(queue.getStatus());
        dto.setQueueDate(queue.getQueueDate());
        dto.setReasonCodes(queue.getReasonCodes());
        return dto;
    }
}