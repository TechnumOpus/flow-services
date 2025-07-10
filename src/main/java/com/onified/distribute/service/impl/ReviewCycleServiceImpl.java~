package com.onified.distribute.service.impl;

import com.onified.distribute.dto.ReviewCycleDTO;
import com.onified.distribute.entity.ReviewCycle;
import com.onified.distribute.repository.ReviewCycleRepository;
import com.onified.distribute.service.ReviewCycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCycleServiceImpl implements ReviewCycleService {

    private final ReviewCycleRepository reviewCycleRepository;

    @Override
    public ReviewCycleDTO createReviewCycle(ReviewCycleDTO reviewCycleDto) {
        log.info("Creating review cycle: {}", reviewCycleDto.getCycleName());
        
        if (reviewCycleRepository.existsByCycleId(reviewCycleDto.getCycleId())) {
            throw new IllegalArgumentException("Review cycle ID already exists: " + reviewCycleDto.getCycleId());
        }
        
        ReviewCycle reviewCycle = mapToEntity(reviewCycleDto);
        reviewCycle.setCreatedAt(LocalDateTime.now());
        reviewCycle.setUpdatedAt(LocalDateTime.now());
        
        if (reviewCycle.getCycleId() == null) {
            reviewCycle.setCycleId("CYCLE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        // Calculate next dates if auto-calculate is enabled
        if (reviewCycle.getAutoCalculateNext() != null && reviewCycle.getAutoCalculateNext()) {
            calculateNextDatesForEntity(reviewCycle);
        }
        
        ReviewCycle savedReviewCycle = reviewCycleRepository.save(reviewCycle);
        log.info("Review cycle created successfully with ID: {}", savedReviewCycle.getCycleId());
        
        return mapToDto(savedReviewCycle);
    }

    @Override
    public ReviewCycleDTO updateReviewCycle(String cycleId, ReviewCycleDTO reviewCycleDto) {
        log.info("Updating review cycle: {}", cycleId);
        
        ReviewCycle existingReviewCycle = reviewCycleRepository.findByCycleId(cycleId)
            .orElseThrow(() -> new IllegalArgumentException("Review cycle not found: " + cycleId));
        
        updateEntityFromDto(existingReviewCycle, reviewCycleDto);
        existingReviewCycle.setUpdatedAt(LocalDateTime.now());
        
        // Recalculate next dates if auto-calculate is enabled
        if (existingReviewCycle.getAutoCalculateNext() != null && existingReviewCycle.getAutoCalculateNext()) {
            calculateNextDatesForEntity(existingReviewCycle);
        }
        
        ReviewCycle savedReviewCycle = reviewCycleRepository.save(existingReviewCycle);
        log.info("Review cycle updated successfully: {}", cycleId);
        
        return mapToDto(savedReviewCycle);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewCycleDTO getReviewCycleByCycleId(String cycleId) {
        ReviewCycle reviewCycle = reviewCycleRepository.findByCycleId(cycleId)
            .orElseThrow(() -> new IllegalArgumentException("Review cycle not found: " + cycleId));
        return mapToDto(reviewCycle);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewCycleDTO> getAllReviewCycles(Pageable pageable) {
        return reviewCycleRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewCycleDTO> getActiveReviewCycles(Pageable pageable) {
        return reviewCycleRepository.findByIsActive(true, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewCycleDTO> getReviewCyclesByFrequency(String frequency, Pageable pageable) {
        return reviewCycleRepository.findByFrequency(frequency, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewCycleDTO> getUpcomingReviewCycles(LocalDateTime fromDate) {
        return reviewCycleRepository.findUpcomingReviewCycles(fromDate)
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Override
    public ReviewCycleDTO activateReviewCycle(String cycleId) {
        log.info("Activating review cycle: {}", cycleId);
        
        ReviewCycle reviewCycle = reviewCycleRepository.findByCycleId(cycleId)
            .orElseThrow(() -> new IllegalArgumentException("Review cycle not found: " + cycleId));
        
        reviewCycle.setIsActive(true);
        reviewCycle.setUpdatedAt(LocalDateTime.now());
        
        ReviewCycle savedReviewCycle = reviewCycleRepository.save(reviewCycle);
        log.info("Review cycle activated successfully: {}", cycleId);
        
        return mapToDto(savedReviewCycle);
    }

    @Override
    public ReviewCycleDTO deactivateReviewCycle(String cycleId) {
        log.info("Deactivating review cycle: {}", cycleId);
        
        ReviewCycle reviewCycle = reviewCycleRepository.findByCycleId(cycleId)
            .orElseThrow(() -> new IllegalArgumentException("Review cycle not found: " + cycleId));
        
        reviewCycle.setIsActive(false);
        reviewCycle.setUpdatedAt(LocalDateTime.now());
        
        ReviewCycle savedReviewCycle = reviewCycleRepository.save(reviewCycle);
        log.info("Review cycle deactivated successfully: {}", cycleId);
        
        return mapToDto(savedReviewCycle);
    }

    @Override
    public void deleteReviewCycle(String cycleId) {
        log.info("Deleting review cycle: {}", cycleId);
        
        ReviewCycle reviewCycle = reviewCycleRepository.findByCycleId(cycleId)
            .orElseThrow(() -> new IllegalArgumentException("Review cycle not found: " + cycleId));
        
        reviewCycleRepository.delete(reviewCycle);
        log.info("Review cycle deleted successfully: {}", cycleId);
    }

    @Override
    public ReviewCycleDTO calculateNextDates(String cycleId) {
        log.info("Calculating next dates for review cycle: {}", cycleId);
        
        ReviewCycle reviewCycle = reviewCycleRepository.findByCycleId(cycleId)
            .orElseThrow(() -> new IllegalArgumentException("Review cycle not found: " + cycleId));
        
        calculateNextDatesForEntity(reviewCycle);
        reviewCycle.setUpdatedAt(LocalDateTime.now());
        
        ReviewCycle savedReviewCycle = reviewCycleRepository.save(reviewCycle);
        log.info("Next dates calculated for review cycle: {}", cycleId);
        
        return mapToDto(savedReviewCycle);
    }

    private void calculateNextDatesForEntity(ReviewCycle reviewCycle) {
        LocalDateTime now = LocalDateTime.now();
        String frequency = reviewCycle.getFrequency();
        
        if (frequency == null) {
            return;
        }
        
        LocalDateTime nextStart;
        LocalDateTime nextEnd;
        
        switch (frequency.toLowerCase()) {
            case "daily":
                nextStart = now.plusDays(1);
                nextEnd = nextStart.plusDays(1);
                break;
            case "weekly":
                nextStart = now.plusWeeks(1);
                nextEnd = nextStart.plusWeeks(1);
                break;
            case "monthly":
                nextStart = now.plusMonths(1);
                nextEnd = nextStart.plusMonths(1);
                break;
            case "quarterly":
                nextStart = now.plusMonths(3);
                nextEnd = nextStart.plusMonths(3);
                break;
            default:
                log.warn("Unknown frequency: {}, using weekly as default", frequency);
                nextStart = now.plusWeeks(1);
                nextEnd = nextStart.plusWeeks(1);
        }
        
        reviewCycle.setNextStartDate(nextStart);
        reviewCycle.setNextEndDate(nextEnd);
    }

    private ReviewCycle mapToEntity(ReviewCycleDTO dto) {
        ReviewCycle reviewCycle = new ReviewCycle();
        reviewCycle.setCycleId(dto.getCycleId());
        reviewCycle.setCycleName(dto.getCycleName());
        reviewCycle.setDescription(dto.getDescription());
        reviewCycle.setStartDay(dto.getStartDay());
        reviewCycle.setEndDay(dto.getEndDay());
        reviewCycle.setFrequency(dto.getFrequency());
        reviewCycle.setNextStartDate(dto.getNextStartDate());
        reviewCycle.setNextEndDate(dto.getNextEndDate());
        reviewCycle.setAutoCalculateNext(dto.getAutoCalculateNext() != null ? dto.getAutoCalculateNext() : true);
        reviewCycle.setTimezone(dto.getTimezone());
        reviewCycle.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        reviewCycle.setCreatedBy(dto.getCreatedBy());
        return reviewCycle;
    }

    private ReviewCycleDTO mapToDto(ReviewCycle entity) {
        ReviewCycleDTO dto = new ReviewCycleDTO();
        dto.setId(entity.getId());
        dto.setCycleId(entity.getCycleId());
        dto.setCycleName(entity.getCycleName());
        dto.setDescription(entity.getDescription());
        dto.setStartDay(entity.getStartDay());
        dto.setEndDay(entity.getEndDay());
        dto.setFrequency(entity.getFrequency());
        dto.setNextStartDate(entity.getNextStartDate());
        dto.setNextEndDate(entity.getNextEndDate());
        dto.setAutoCalculateNext(entity.getAutoCalculateNext());
        dto.setTimezone(entity.getTimezone());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        return dto;
    }

    private void updateEntityFromDto(ReviewCycle entity, ReviewCycleDTO dto) {
        entity.setCycleName(dto.getCycleName());
        entity.setDescription(dto.getDescription());
        entity.setStartDay(dto.getStartDay());
        entity.setEndDay(dto.getEndDay());
        entity.setFrequency(dto.getFrequency());
        entity.setNextStartDate(dto.getNextStartDate());
        entity.setNextEndDate(dto.getNextEndDate());
        if (dto.getAutoCalculateNext() != null) {
            entity.setAutoCalculateNext(dto.getAutoCalculateNext());
        }
        entity.setTimezone(dto.getTimezone());
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }
}
