package com.onified.distribute.service.impl.masterdata;

import com.onified.distribute.dto.SpecialEventDTO;
import com.onified.distribute.entity.SpecialEvent;
import com.onified.distribute.repository.SpecialEventRepository;
import com.onified.distribute.service.masterdata.SpecialEventService;
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
public class SpecialEventServiceImpl implements SpecialEventService {

    private final SpecialEventRepository specialEventRepository;

    @Override
    public SpecialEventDTO createSpecialEvent(SpecialEventDTO eventDto) {
        log.info("Creating special event: {}", eventDto.getEventName());

        if (eventDto.getEventId() != null && specialEventRepository.existsByEventId(eventDto.getEventId())) {
            throw new IllegalArgumentException("Event ID already exists: " + eventDto.getEventId());
        }

        // Check for overlapping events
        if (eventDto.getProductId() != null && eventDto.getLocationId() != null) {
            List<SpecialEvent> overlappingEvents = specialEventRepository.findOverlappingEvents(
                    eventDto.getProductId(), eventDto.getLocationId(),
                    eventDto.getStartDate(), eventDto.getEndDate());

            if (!overlappingEvents.isEmpty()) {
                log.warn("Found {} overlapping events for product {} at location {}",
                        overlappingEvents.size(), eventDto.getProductId(), eventDto.getLocationId());
            }
        }

        SpecialEvent event = mapToEntity(eventDto);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        if (event.getEventId() == null) {
            event.setEventId("EVENT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        if (event.getApprovalStatus() == null) {
            event.setApprovalStatus("PENDING");
        }

        SpecialEvent savedEvent = specialEventRepository.save(event);
        log.info("Special event created successfully with ID: {}", savedEvent.getEventId());

        return mapToDto(savedEvent);
    }

    @Override
    public SpecialEventDTO updateSpecialEvent(String eventId, SpecialEventDTO eventDto) {
        log.info("Updating special event: {}", eventId);

        SpecialEvent existingEvent = specialEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Special event not found: " + eventId));

        updateEntityFromDto(existingEvent, eventDto);
        existingEvent.setUpdatedAt(LocalDateTime.now());

        SpecialEvent savedEvent = specialEventRepository.save(existingEvent);
        log.info("Special event updated successfully: {}", eventId);

        return mapToDto(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialEventDTO getSpecialEventById(String eventId) {
        SpecialEvent event = specialEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Special event not found: " + eventId));
        return mapToDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpecialEventDTO> getAllSpecialEvents(Pageable pageable) {
        return specialEventRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpecialEventDTO> getActiveSpecialEvents(Pageable pageable) {
        return specialEventRepository.findActiveEvents(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpecialEventDTO> getSpecialEventsByProduct(String productId, Pageable pageable) {
        return specialEventRepository.findByProductId(productId, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpecialEventDTO> getSpecialEventsByLocation(String locationId, Pageable pageable) {
        return specialEventRepository.findByLocationId(locationId, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpecialEventDTO> getCurrentEvents(LocalDateTime currentDate, Pageable pageable) {
        return specialEventRepository.findCurrentEvents(currentDate, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpecialEventDTO> getUpcomingEvents(LocalDateTime currentDate, Pageable pageable) {
        return specialEventRepository.findUpcomingEvents(currentDate, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialEventDTO> getOverlappingEvents(String productId, String locationId,
                                                      LocalDateTime startDate, LocalDateTime endDate) {
        return specialEventRepository.findOverlappingEvents(productId, locationId, startDate, endDate)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getEventImpactFactor(String productId, String locationId, LocalDateTime date) {
        List<SpecialEvent> currentEvents = specialEventRepository.findCurrentEvents(date, null).getContent();

        return currentEvents.stream()
                .filter(event -> (productId == null || productId.equals(event.getProductId())) &&
                        (locationId == null || locationId.equals(event.getLocationId())))
                .mapToDouble(event -> event.getChangeFactor() != null ? event.getChangeFactor() : 1.0)
                .max()
                .orElse(1.0);
    }

    @Override
    public SpecialEventDTO approveEvent(String eventId, String approvedBy) {
        log.info("Approving special event: {} by {}", eventId, approvedBy);

        SpecialEvent event = specialEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Special event not found: " + eventId));

        event.setApprovalStatus("APPROVED");
        event.setApprovedBy(approvedBy);
        event.setUpdatedAt(LocalDateTime.now());

        SpecialEvent savedEvent = specialEventRepository.save(event);
        log.info("Special event approved successfully: {}", eventId);

        return mapToDto(savedEvent);
    }

    @Override
    public SpecialEventDTO rejectEvent(String eventId, String approvedBy) {
        log.info("Rejecting special event: {} by {}", eventId, approvedBy);

        SpecialEvent event = specialEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Special event not found: " + eventId));

        event.setApprovalStatus("REJECTED");
        event.setApprovedBy(approvedBy);
        event.setUpdatedAt(LocalDateTime.now());

        SpecialEvent savedEvent = specialEventRepository.save(event);
        log.info("Special event rejected successfully: {}", eventId);

        return mapToDto(savedEvent);
    }

    @Override
    public SpecialEventDTO activateSpecialEvent(String eventId) {
        log.info("Activating special event: {}", eventId);

        SpecialEvent event = specialEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Special event not found: " + eventId));

        event.setIsActive(true);
        event.setUpdatedAt(LocalDateTime.now());

        SpecialEvent savedEvent = specialEventRepository.save(event);
        log.info("Special event activated successfully: {}", eventId);

        return mapToDto(savedEvent);
    }

    @Override
    public SpecialEventDTO deactivateSpecialEvent(String eventId) {
        log.info("Deactivating special event: {}", eventId);

        SpecialEvent event = specialEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Special event not found: " + eventId));

        event.setIsActive(false);
        event.setUpdatedAt(LocalDateTime.now());

        SpecialEvent savedEvent = specialEventRepository.save(event);
        log.info("Special event deactivated successfully: {}", eventId);

        return mapToDto(savedEvent);
    }

    @Override
    public void deleteSpecialEvent(String eventId) {
        log.info("Deleting special event: {}", eventId);

        SpecialEvent event = specialEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Special event not found: " + eventId));

        specialEventRepository.delete(event);
        log.info("Special event deleted successfully: {}", eventId);
    }

    @Override
    public SpecialEventDTO createEvent(SpecialEventDTO eventDto) {
        return createSpecialEvent(eventDto); // Reuse existing logic
    }

    @Override
    public SpecialEventDTO updateEvent(String eventId, SpecialEventDTO eventDto) {
        return updateSpecialEvent(eventId, eventDto); // Reuse existing logic
    }

    @Override
    public void deleteEvent(String eventId) {
        deleteSpecialEvent(eventId); // Reuse existing logic
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpecialEventDTO> getEvents(String category, String locationId, LocalDateTime startDate, LocalDateTime endDate, Boolean isActive, Pageable pageable) {
        log.info("Fetching events with filters: category={}, locationId={}, startDate={}, endDate={}, isActive={}",
                category, locationId, startDate, endDate, isActive);
        return specialEventRepository.findEvents(category, locationId, startDate, endDate, isActive, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpecialEventDTO> getEventsByCalendar(Integer month, Integer year, Pageable pageable) {
        log.info("Fetching events for month: {} and year: {}", month, year);
        return specialEventRepository.findEventsByCalendar(month, year, pageable)
                .map(this::mapToDto);
    }

    private SpecialEvent mapToEntity(SpecialEventDTO dto) {
        SpecialEvent event = new SpecialEvent();
        event.setEventId(dto.getEventId());
        event.setEventName(dto.getEventName());
        event.setEventDescription(dto.getEventDescription());
        event.setProductId(dto.getProductId());
        event.setLocationId(dto.getLocationId());
        event.setCategory(dto.getCategory());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setChangeFactor(dto.getChangeFactor());
        event.setEventType(dto.getEventType());
        event.setApprovalStatus(dto.getApprovalStatus());
        event.setApprovedBy(dto.getApprovedBy());
        event.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        event.setAffectedSkus(dto.getAffectedSkus()); // Added for SKU list
        event.setCreatedBy(dto.getCreatedBy());
        event.setApprovedBy(dto.getApprovedBy());
        return event;
    }

    private SpecialEventDTO mapToDto(SpecialEvent entity) {
        SpecialEventDTO dto = new SpecialEventDTO();
        dto.setId(entity.getId());
        dto.setEventId(entity.getEventId());
        dto.setEventName(entity.getEventName());
        dto.setEventDescription(entity.getEventDescription());
        dto.setProductId(entity.getProductId());
        dto.setLocationId(entity.getLocationId());
        dto.setCategory(entity.getCategory());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setChangeFactor(entity.getChangeFactor());
        dto.setEventType(entity.getEventType());
        dto.setApprovalStatus(entity.getApprovalStatus());
        dto.setApprovedBy(entity.getApprovedBy());
        dto.setIsActive(entity.getIsActive());
        dto.setAffectedSkus(entity.getAffectedSkus()); // Added for SKU list
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setApprovedBy(entity.getApprovedBy());
        return dto;
    }

    private void updateEntityFromDto(SpecialEvent entity, SpecialEventDTO dto) {
        entity.setEventName(dto.getEventName());
        entity.setEventDescription(dto.getEventDescription());
        entity.setProductId(dto.getProductId());
        entity.setLocationId(dto.getLocationId());
        entity.setCategory(dto.getCategory());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setChangeFactor(dto.getChangeFactor());
        entity.setEventType(dto.getEventType());
        entity.setApprovalStatus(dto.getApprovalStatus());
        entity.setApprovedBy(dto.getApprovedBy());
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
        entity.setAffectedSkus(dto.getAffectedSkus()); // Added for SKU list
        entity.setApprovedBy(dto.getApprovedBy());
    }
}