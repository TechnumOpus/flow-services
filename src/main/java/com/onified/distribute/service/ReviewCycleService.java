package com.onified.distribute.service;

import com.onified.distribute.dto.ReviewCycleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewCycleService {
    ReviewCycleDTO createReviewCycle(ReviewCycleDTO reviewCycleDto);
    ReviewCycleDTO updateReviewCycle(String cycleId, ReviewCycleDTO reviewCycleDto);
    ReviewCycleDTO getReviewCycleByCycleId(String cycleId);
    Page<ReviewCycleDTO> getAllReviewCycles(Pageable pageable);
    Page<ReviewCycleDTO> getActiveReviewCycles(Pageable pageable);
    Page<ReviewCycleDTO> getReviewCyclesByFrequency(String frequency, Pageable pageable);
    List<ReviewCycleDTO> getUpcomingReviewCycles(LocalDateTime fromDate);
    ReviewCycleDTO activateReviewCycle(String cycleId);
    ReviewCycleDTO deactivateReviewCycle(String cycleId);
    void deleteReviewCycle(String cycleId);
    ReviewCycleDTO calculateNextDates(String cycleId);
}
