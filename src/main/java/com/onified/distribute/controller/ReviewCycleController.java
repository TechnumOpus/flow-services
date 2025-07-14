package com.onified.distribute.controller;

import com.onified.distribute.dto.ReviewCycleDTO;
import com.onified.distribute.service.dbm.ReviewCycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/review-cycles")
@RequiredArgsConstructor
@Validated
public class ReviewCycleController {

    private final ReviewCycleService reviewCycleService;

    @PostMapping
    public ResponseEntity<ReviewCycleDTO> createReviewCycle(@Valid @RequestBody ReviewCycleDTO reviewCycleDto) {
        log.info("Creating review cycle: {}", reviewCycleDto.getCycleName());
        ReviewCycleDTO createdReviewCycle = reviewCycleService.createReviewCycle(reviewCycleDto);
        return new ResponseEntity<>(createdReviewCycle, HttpStatus.CREATED);
    }

    @PutMapping("/{cycleId}")
    public ResponseEntity<ReviewCycleDTO> updateReviewCycle(
            @PathVariable String cycleId,
            @Valid @RequestBody ReviewCycleDTO reviewCycleDto) {
        log.info("Updating review cycle: {}", cycleId);
        ReviewCycleDTO updatedReviewCycle = reviewCycleService.updateReviewCycle(cycleId, reviewCycleDto);
        return ResponseEntity.ok(updatedReviewCycle);
    }

    @GetMapping("/{cycleId}")
    public ResponseEntity<ReviewCycleDTO> getReviewCycleByCycleId(@PathVariable String cycleId) {
        log.info("Fetching review cycle: {}", cycleId);
        ReviewCycleDTO reviewCycle = reviewCycleService.getReviewCycleByCycleId(cycleId);
        return ResponseEntity.ok(reviewCycle);
    }

    @GetMapping
    public ResponseEntity<Page<ReviewCycleDTO>> getAllReviewCycles(Pageable pageable) {
        log.info("Fetching all review cycles with pagination");
        Page<ReviewCycleDTO> reviewCycles = reviewCycleService.getAllReviewCycles(pageable);
        return ResponseEntity.ok(reviewCycles);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<ReviewCycleDTO>> getActiveReviewCycles(Pageable pageable) {
        log.info("Fetching active review cycles with pagination");
        Page<ReviewCycleDTO> reviewCycles = reviewCycleService.getActiveReviewCycles(pageable);
        return ResponseEntity.ok(reviewCycles);
    }

    @GetMapping("/frequency/{frequency}")
    public ResponseEntity<Page<ReviewCycleDTO>> getReviewCyclesByFrequency(
            @PathVariable String frequency,
            Pageable pageable) {
        log.info("Fetching review cycles by frequency: {}", frequency);
        Page<ReviewCycleDTO> reviewCycles = reviewCycleService.getReviewCyclesByFrequency(frequency, pageable);
        return ResponseEntity.ok(reviewCycles);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ReviewCycleDTO>> getUpcomingReviewCycles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate) {
        LocalDateTime searchDate = fromDate != null ? fromDate : LocalDateTime.now();
        log.info("Fetching upcoming review cycles from: {}", searchDate);
        List<ReviewCycleDTO> reviewCycles = reviewCycleService.getUpcomingReviewCycles(searchDate);
        return ResponseEntity.ok(reviewCycles);
    }

    @PatchMapping("/{cycleId}/activate")
    public ResponseEntity<ReviewCycleDTO> activateReviewCycle(@PathVariable String cycleId) {
        log.info("Activating review cycle: {}", cycleId);
        ReviewCycleDTO reviewCycle = reviewCycleService.activateReviewCycle(cycleId);
        return ResponseEntity.ok(reviewCycle);
    }

    @PatchMapping("/{cycleId}/deactivate")
    public ResponseEntity<ReviewCycleDTO> deactivateReviewCycle(@PathVariable String cycleId) {
        log.info("Deactivating review cycle: {}", cycleId);
        ReviewCycleDTO reviewCycle = reviewCycleService.deactivateReviewCycle(cycleId);
        return ResponseEntity.ok(reviewCycle);
    }

    @DeleteMapping("/{cycleId}")
    public ResponseEntity<Void> deleteReviewCycle(@PathVariable String cycleId) {
        log.info("Deleting review cycle: {}", cycleId);
        reviewCycleService.deleteReviewCycle(cycleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cycleId}/calculate-next-dates")
    public ResponseEntity<ReviewCycleDTO> calculateNextDates(@PathVariable String cycleId) {
        log.info("Calculating next dates for review cycle: {}", cycleId);
        ReviewCycleDTO reviewCycle = reviewCycleService.calculateNextDates(cycleId);
        return ResponseEntity.ok(reviewCycle);
    }
}
