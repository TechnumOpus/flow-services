package com.onified.distribute.service.dbm;

import com.onified.distribute.dto.BufferReviewDTO;
import com.onified.distribute.dto.BufferAdjustmentRequestDTO;
import com.onified.distribute.dto.BufferAdjustmentLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BufferReviewService {

    /**
     * Get buffer review data with filters
     */
    Page<BufferReviewDTO> getBufferReviewData(String productId, String locationId,
                                              String currentZone, Boolean dueForReview,
                                              Pageable pageable);

    /**
     * Get buffer review data for a specific buffer
     */
    BufferReviewDTO getBufferReviewById(String bufferId);

    /**
     * Adjust buffer and log the adjustment
     */
    BufferAdjustmentLogDTO adjustBuffer(BufferAdjustmentRequestDTO request);

    /**
     * Calculate DBM recommendations for all buffers
     */
    List<BufferReviewDTO> calculateDbmRecommendations();

    /**
     * Calculate DBM recommendation for a specific buffer
     */
    BufferReviewDTO calculateDbmRecommendation(String bufferId);

    /**
     * Get buffers due for review
     */
    Page<BufferReviewDTO> getBuffersDueForReview(Pageable pageable);

    /**
     * Bulk adjust buffers
     */
    List<BufferAdjustmentLogDTO> bulkAdjustBuffers(List<BufferAdjustmentRequestDTO> requests);
}
