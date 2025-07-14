package com.onified.distribute.controller;

import com.onified.distribute.dto.SpecialEventDTO;
import com.onified.distribute.service.masterdata.SpecialEventService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@RequestMapping("/api/v1/special-events")
@RequiredArgsConstructor
@Validated
public class SpecialEventController {

    private final SpecialEventService specialEventService;

    @PostMapping
    public ResponseEntity<SpecialEventDTO> createSpecialEvent(@Valid @RequestBody SpecialEventDTO eventDto) {
        log.info("Creating special event: {}", eventDto.getEventName());
        SpecialEventDTO createdEvent = specialEventService.createSpecialEvent(eventDto);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<SpecialEventDTO> updateSpecialEvent(
            @PathVariable String eventId,
            @Valid @RequestBody SpecialEventDTO eventDto) {
        log.info("Updating special event: {}", eventId);
        SpecialEventDTO updatedEvent = specialEventService.updateSpecialEvent(eventId, eventDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<SpecialEventDTO> getSpecialEventById(@PathVariable String eventId) {
        log.info("Fetching special event: {}", eventId);
        SpecialEventDTO event = specialEventService.getSpecialEventById(eventId);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<Page<SpecialEventDTO>> getAllSpecialEvents(Pageable pageable) {
        log.info("Fetching all special events with pagination");
        Page<SpecialEventDTO> events = specialEventService.getAllSpecialEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<SpecialEventDTO>> getActiveSpecialEvents(Pageable pageable) {
        log.info("Fetching active special events with pagination");
        Page<SpecialEventDTO> events = specialEventService.getActiveSpecialEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<SpecialEventDTO>> getSpecialEventsByProduct(
            @PathVariable String productId,
            Pageable pageable) {
        log.info("Fetching special events by product: {}", productId);
        Page<SpecialEventDTO> events = specialEventService.getSpecialEventsByProduct(productId, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<Page<SpecialEventDTO>> getSpecialEventsByLocation(
            @PathVariable String locationId,
            Pageable pageable) {
        log.info("Fetching special events by location: {}", locationId);
        Page<SpecialEventDTO> events = specialEventService.getSpecialEventsByLocation(locationId, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/current")
    public ResponseEntity<Page<SpecialEventDTO>> getCurrentEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime currentDate,
            Pageable pageable) {
        LocalDateTime searchDate = currentDate != null ? currentDate : LocalDateTime.now();
        log.info("Fetching current special events for date: {}", searchDate);
        Page<SpecialEventDTO> events = specialEventService.getCurrentEvents(searchDate, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<SpecialEventDTO>> getUpcomingEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime currentDate,
            Pageable pageable) {
        LocalDateTime searchDate = currentDate != null ? currentDate : LocalDateTime.now();
        log.info("Fetching upcoming special events from date: {}", searchDate);
        Page<SpecialEventDTO> events = specialEventService.getUpcomingEvents(searchDate, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/overlapping")
    public ResponseEntity<List<SpecialEventDTO>> getOverlappingEvents(
            @RequestParam String productId,
            @RequestParam String locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching overlapping events for product: {} at location: {} between {} and {}",
                productId, locationId, startDate, endDate);
        List<SpecialEventDTO> events = specialEventService.getOverlappingEvents(productId, locationId, startDate, endDate);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/product/{productId}/location/{locationId}/impact-factor")
    public ResponseEntity<Double> getEventImpactFactor(
            @PathVariable String productId,
            @PathVariable String locationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        LocalDateTime searchDate = date != null ? date : LocalDateTime.now();
        log.info("Fetching event impact factor for product: {} at location: {} for date: {}",
                productId, locationId, searchDate);
        Double impactFactor = specialEventService.getEventImpactFactor(productId, locationId, searchDate);
        return ResponseEntity.ok(impactFactor);
    }

    @PatchMapping("/{eventId}/approve")
    public ResponseEntity<SpecialEventDTO> approveEvent(
            @PathVariable String eventId,
            @RequestParam String approvedBy) {
        log.info("Approving special event: {} by {}", eventId, approvedBy);
        SpecialEventDTO event = specialEventService.approveEvent(eventId, approvedBy);
        return ResponseEntity.ok(event);
    }

    @PatchMapping("/{eventId}/reject")
    public ResponseEntity<SpecialEventDTO> rejectEvent(
            @PathVariable String eventId,
            @RequestParam String approvedBy) {
        log.info("Rejecting special event: {} by {}", eventId, approvedBy);
        SpecialEventDTO event = specialEventService.rejectEvent(eventId, approvedBy);
        return ResponseEntity.ok(event);
    }

    @PatchMapping("/{eventId}/activate")
    public ResponseEntity<SpecialEventDTO> activateSpecialEvent(@PathVariable String eventId) {
        log.info("Activating special event: {}", eventId);
        SpecialEventDTO event = specialEventService.activateSpecialEvent(eventId);
        return ResponseEntity.ok(event);
    }

    @PatchMapping("/{eventId}/deactivate")
    public ResponseEntity<SpecialEventDTO> deactivateSpecialEvent(@PathVariable String eventId) {
        log.info("Deactivating special event: {}", eventId);
        SpecialEventDTO event = specialEventService.deactivateSpecialEvent(eventId);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteSpecialEvent(@PathVariable String eventId) {
        log.info("Deleting special event: {}", eventId);
        specialEventService.deleteSpecialEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events")
    public ResponseEntity<SpecialEventDTO> createEvent(@Valid @RequestBody SpecialEventDTO eventDto) {
        log.info("Creating event: {}", eventDto.getEventName());
        SpecialEventDTO createdEvent = specialEventService.createEvent(eventDto);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<SpecialEventDTO> updateEvent(
            @PathVariable String eventId,
            @Valid @RequestBody SpecialEventDTO eventDto) {
        log.info("Updating event: {}", eventId);
        SpecialEventDTO updatedEvent = specialEventService.updateEvent(eventId, eventDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String eventId) {
        log.info("Deleting event: {}", eventId);
        specialEventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events")
    public ResponseEntity<Page<SpecialEventDTO>> getEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        log.info("Fetching events with filters: category={}, locationId={}, startDate={}, endDate={}, isActive={}",
                category, locationId, startDate, endDate, isActive);
        Page<SpecialEventDTO> events = specialEventService.getEvents(category, locationId, startDate, endDate, isActive, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/calendar")
    public ResponseEntity<Page<SpecialEventDTO>> getEventsByCalendar(
            @RequestParam @Min(1) @Max(12) Integer month,
            @RequestParam Integer year,
            Pageable pageable) {
        log.info("Fetching events for month: {} and year: {}", month, year);
        Page<SpecialEventDTO> events = specialEventService.getEventsByCalendar(month, year, pageable);
        return ResponseEntity.ok(events);
    }
}