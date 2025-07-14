package com.onified.distribute.service.masterdata;

import com.onified.distribute.dto.SpecialEventDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface SpecialEventService {
    SpecialEventDTO createSpecialEvent(SpecialEventDTO eventDto);
    SpecialEventDTO getSpecialEventById(String eventId);
    Page<SpecialEventDTO> getAllSpecialEvents(Pageable pageable);
    Page<SpecialEventDTO> getActiveSpecialEvents(Pageable pageable);
    Page<SpecialEventDTO> getSpecialEventsByProduct(String productId, Pageable pageable);
    Page<SpecialEventDTO> getSpecialEventsByLocation(String locationId, Pageable pageable);
    Page<SpecialEventDTO> getCurrentEvents(LocalDateTime currentDate, Pageable pageable);
    Page<SpecialEventDTO> getUpcomingEvents(LocalDateTime currentDate, Pageable pageable);
    List<SpecialEventDTO> getOverlappingEvents(String productId, String locationId, LocalDateTime startDate, LocalDateTime endDate);
    Double getEventImpactFactor(String productId, String locationId, LocalDateTime date);
    SpecialEventDTO approveEvent(String eventId, String approvedBy);
    SpecialEventDTO rejectEvent(String eventId, String approvedBy);
    SpecialEventDTO activateSpecialEvent(String eventId);
    SpecialEventDTO deactivateSpecialEvent(String eventId);
    void deleteSpecialEvent(String eventId);
    SpecialEventDTO updateSpecialEvent(String eventId, SpecialEventDTO eventDto);
    SpecialEventDTO createEvent(SpecialEventDTO eventDto);
    SpecialEventDTO updateEvent(String eventId, SpecialEventDTO eventDto);
    void deleteEvent(String eventId);
    Page<SpecialEventDTO> getEvents(String category, String locationId, LocalDateTime startDate, LocalDateTime endDate, Boolean isActive, Pageable pageable);
    Page<SpecialEventDTO> getEventsByCalendar(Integer month, Integer year, Pageable pageable);
}