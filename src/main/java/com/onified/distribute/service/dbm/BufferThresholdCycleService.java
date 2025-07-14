package com.onified.distribute.service.dbm;

import com.onified.distribute.dto.BufferThresholdCycleDTO;
import com.onified.distribute.dto.BufferThresholdUpdateDTO;
import com.onified.distribute.dto.ReviewCycleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BufferThresholdCycleService {

    /**
     * Get all inventory buffers with threshold and cycle information
     * @param productId Optional filter by product ID
     * @param locationId Optional filter by location ID
     * @param pageable Pagination information
     * @return Page of BufferThresholdCycleDTO
     */
    Page<BufferThresholdCycleDTO> getBufferThresholdCycles(String productId, String locationId, Pageable pageable);

    /**
     * Get buffer threshold and cycle information by buffer ID
     * @param bufferId Buffer ID
     * @return BufferThresholdCycleDTO
     */
    BufferThresholdCycleDTO getBufferThresholdCycleById(String bufferId);

    /**
     * Update buffer threshold and cycle settings
     * @param bufferId Buffer ID to update
     * @param updateDTO Update data
     * @return Updated BufferThresholdCycleDTO
     */
    BufferThresholdCycleDTO updateBufferThresholdCycle(String bufferId, BufferThresholdUpdateDTO updateDTO);

    /**
     * Get all available review cycles for dropdown
     * @return List of review cycles
     */
    List<ReviewCycleDTO> getAvailableReviewCycles();

    /**
     * Trigger manual review for a specific buffer
     * @param bufferId The ID of the buffer to trigger review for
     */
    void triggerBufferReview(String bufferId);



}
