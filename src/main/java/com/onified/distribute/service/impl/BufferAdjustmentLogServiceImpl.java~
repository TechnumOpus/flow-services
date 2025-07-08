package com.onified.distribute.service.impl;

import com.onified.distribute.dto.BufferAdjustmentLogDTO;
import com.onified.distribute.entity.BufferAdjustmentLog;
import com.onified.distribute.repository.BufferAdjustmentLogRepository;
import com.onified.distribute.service.BufferAdjustmentLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BufferAdjustmentLogServiceImpl implements BufferAdjustmentLogService {
    
    private final BufferAdjustmentLogRepository bufferAdjustmentLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BufferAdjustmentLogDTO> getBufferAdjustmentLogsByCreatedBy(String createdBy, Pageable pageable) {
        log.info("Fetching buffer adjustment logs by creator: {}", createdBy);
        return bufferAdjustmentLogRepository.findByCreatedBy(createdBy, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BufferAdjustmentLogDTO> getBufferAdjustmentLogsByApprovedBy(String approvedBy, Pageable pageable) {
        log.info("Fetching buffer adjustment logs by approver: {}", approvedBy);
        return bufferAdjustmentLogRepository.findByApprovedBy(approvedBy, pageable).map(this::convertToDto);
    }


    @Override
    public BufferAdjustmentLogDTO createBufferAdjustmentLog(BufferAdjustmentLogDTO logDto) {
        log.info("Creating buffer adjustment log for buffer: {}", logDto.getBufferId());
        
        BufferAdjustmentLog adjustmentLog = mapToEntity(logDto);
        adjustmentLog.setLogId(UUID.randomUUID().toString());
        adjustmentLog.setAdjustmentDate(LocalDateTime.now());
        
        if (adjustmentLog.getApprovalStatus() == null) {
            adjustmentLog.setApprovalStatus("PENDING");
        }
        
        BufferAdjustmentLog savedLog = bufferAdjustmentLogRepository.save(adjustmentLog);
        return convertToDto(savedLog);
    }

    @Override
    public BufferAdjustmentLogDTO updateBufferAdjustmentLog(String logId, BufferAdjustmentLogDTO logDto) {
        log.info("Updating buffer adjustment log: {}", logId);
        
        BufferAdjustmentLog existingLog = bufferAdjustmentLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Buffer adjustment log not found with ID: " + logId));
        
        updateEntityFromDto(logDto, existingLog);
        
        BufferAdjustmentLog savedLog = bufferAdjustmentLogRepository.save(existingLog);
        return convertToDto(savedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public BufferAdjustmentLogDTO getBufferAdjustmentLogById(String logId) {
        log.info("Fetching buffer adjustment log: {}", logId);
        
        BufferAdjustmentLog adjustmentLog = bufferAdjustmentLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Buffer adjustment log not found with ID: " + logId));
        
        return convertToDto(adjustmentLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BufferAdjustmentLogDTO> getAllBufferAdjustmentLogs(Pageable pageable) {
        log.info("Fetching all buffer adjustment logs");
        return bufferAdjustmentLogRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BufferAdjustmentLogDTO> getBufferAdjustmentLogsByBuffer(String bufferId, Pageable pageable) {
        log.info("Fetching buffer adjustment logs by buffer: {}", bufferId);
        return bufferAdjustmentLogRepository.findByBufferId(bufferId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BufferAdjustmentLogDTO> getBufferAdjustmentLogsByProductAndLocation(String productId, String locationId, Pageable pageable) {
        log.info("Fetching buffer adjustment logs by product: {} and location: {}", productId, locationId);
        return bufferAdjustmentLogRepository.findByProductIdAndLocationId(productId, locationId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BufferAdjustmentLogDTO> getBufferAdjustmentLogsByApprovalStatus(String approvalStatus, Pageable pageable) {
        log.info("Fetching buffer adjustment logs by approval status: {}", approvalStatus);
        return bufferAdjustmentLogRepository.findByApprovalStatus(approvalStatus, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BufferAdjustmentLogDTO> getBufferAdjustmentLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Fetching buffer adjustment logs between {} and {}", startDate, endDate);
        return bufferAdjustmentLogRepository.findByAdjustmentDateBetween(startDate, endDate, pageable).map(this::convertToDto);
    }

    @Override
    public BufferAdjustmentLogDTO approveAdjustment(String logId, String approvedBy) {
        log.info("Approving buffer adjustment log: {} by: {}", logId, approvedBy);
        
        BufferAdjustmentLog adjustmentLog = bufferAdjustmentLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Buffer adjustment log not found with ID: " + logId));
        
        adjustmentLog.setApprovalStatus("APPROVED");
        adjustmentLog.setApprovedBy(approvedBy);
        adjustmentLog.setApprovalDate(LocalDateTime.now());
        
        BufferAdjustmentLog savedLog = bufferAdjustmentLogRepository.save(adjustmentLog);
        return convertToDto(savedLog);
    }

    @Override
    public BufferAdjustmentLogDTO rejectAdjustment(String logId, String approvedBy) {
        log.info("Rejecting buffer adjustment log: {} by: {}", logId, approvedBy);
        
        BufferAdjustmentLog adjustmentLog = bufferAdjustmentLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Buffer adjustment log not found with ID: " + logId));
        
        adjustmentLog.setApprovalStatus("REJECTED");
        adjustmentLog.setApprovedBy(approvedBy);
        adjustmentLog.setApprovalDate(LocalDateTime.now());
        
        BufferAdjustmentLog savedLog = bufferAdjustmentLogRepository.save(adjustmentLog);
        return convertToDto(savedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BufferAdjustmentLogDTO> getPendingApprovals(Pageable pageable) {
        log.info("Fetching pending buffer adjustment approvals");
        return bufferAdjustmentLogRepository.findPendingApprovals(pageable).map(this::convertToDto);
    }

    @Override
    public BufferAdjustmentLogDTO logBufferAdjustment(String bufferId, String adjustmentType, Integer oldBufferDays,
                                                      Integer newBufferDays, String triggerReason, String createdBy) {
        log.info("Logging buffer adjustment for buffer: {} with type: {}", bufferId, adjustmentType);
        
        BufferAdjustmentLogDTO logDto = new BufferAdjustmentLogDTO();
        logDto.setBufferId(bufferId);
        logDto.setAdjustmentType(adjustmentType);
        logDto.setOldBufferDays(oldBufferDays);
        logDto.setNewBufferDays(newBufferDays);
        logDto.setTriggerReason(triggerReason);
        logDto.setCreatedBy(createdBy);
        logDto.setSystemRecommended(true);
        logDto.setRequiresApproval(Math.abs(newBufferDays - oldBufferDays) > (oldBufferDays * 0.2)); // 20% threshold
        
        // Calculate change percentage
        if (oldBufferDays != null && oldBufferDays > 0) {
            double changePercentage = ((double) (newBufferDays - oldBufferDays) / oldBufferDays) * 100;
            logDto.setChangePercentage(changePercentage);
        }
        
        return createBufferAdjustmentLog(logDto);
    }

    @Override
    public void deleteBufferAdjustmentLog(String logId) {
        log.info("Deleting buffer adjustment log: {}", logId);
        
        if (!bufferAdjustmentLogRepository.existsById(logId)) {
            throw new IllegalArgumentException("Buffer adjustment log not found with ID: " + logId);
        }
        
        bufferAdjustmentLogRepository.deleteById(logId);
    }

    private BufferAdjustmentLogDTO convertToDto(BufferAdjustmentLog adjustmentLog) {
        BufferAdjustmentLogDTO dto = new BufferAdjustmentLogDTO();
        dto.setLogId(adjustmentLog.getLogId());
        dto.setBufferId(adjustmentLog.getBufferId());
        dto.setProductId(adjustmentLog.getProductId());
        dto.setLocationId(adjustmentLog.getLocationId());
        dto.setAdjustmentType(adjustmentLog.getAdjustmentType());
        dto.setOldBufferDays(adjustmentLog.getOldBufferDays());
        dto.setNewBufferDays(adjustmentLog.getNewBufferDays());
        dto.setOldBufferUnits(adjustmentLog.getOldBufferUnits());
        dto.setNewBufferUnits(adjustmentLog.getNewBufferUnits());
        dto.setChangePercentage(adjustmentLog.getChangePercentage());
        dto.setTriggerReason(adjustmentLog.getTriggerReason());
        dto.setConsecutiveDaysInZone(adjustmentLog.getConsecutiveDaysInZone());
        dto.setZoneWhenTriggered(adjustmentLog.getZoneWhenTriggered());
        dto.setAdcAtAdjustment(adjustmentLog.getAdcAtAdjustment());
        dto.setSystemRecommended(adjustmentLog.getSystemRecommended());
        dto.setRequiresApproval(adjustmentLog.getRequiresApproval());
        dto.setApprovalStatus(adjustmentLog.getApprovalStatus());
        dto.setApprovedBy(adjustmentLog.getApprovedBy());
        dto.setApprovalDate(adjustmentLog.getApprovalDate());
        dto.setAdjustmentDate(adjustmentLog.getAdjustmentDate());
        dto.setCreatedBy(adjustmentLog.getCreatedBy());
        dto.setComments(adjustmentLog.getComments());
        return dto;
    }

    private BufferAdjustmentLog mapToEntity(BufferAdjustmentLogDTO dto) {
        BufferAdjustmentLog adjustmentLog = new BufferAdjustmentLog();
        adjustmentLog.setLogId(dto.getLogId());
        adjustmentLog.setBufferId(dto.getBufferId());
        adjustmentLog.setProductId(dto.getProductId());
        adjustmentLog.setLocationId(dto.getLocationId());
        adjustmentLog.setAdjustmentType(dto.getAdjustmentType());
        adjustmentLog.setOldBufferDays(dto.getOldBufferDays());
        adjustmentLog.setNewBufferDays(dto.getNewBufferDays());
        adjustmentLog.setOldBufferUnits(dto.getOldBufferUnits());
        adjustmentLog.setNewBufferUnits(dto.getNewBufferUnits());
        adjustmentLog.setChangePercentage(dto.getChangePercentage());
        adjustmentLog.setTriggerReason(dto.getTriggerReason());
        adjustmentLog.setConsecutiveDaysInZone(dto.getConsecutiveDaysInZone());
        adjustmentLog.setZoneWhenTriggered(dto.getZoneWhenTriggered());
        adjustmentLog.setAdcAtAdjustment(dto.getAdcAtAdjustment());
        adjustmentLog.setSystemRecommended(dto.getSystemRecommended());
        adjustmentLog.setRequiresApproval(dto.getRequiresApproval());
        adjustmentLog.setApprovalStatus(dto.getApprovalStatus());
        adjustmentLog.setApprovedBy(dto.getApprovedBy());
        adjustmentLog.setApprovalDate(dto.getApprovalDate());
        adjustmentLog.setAdjustmentDate(dto.getAdjustmentDate());
        adjustmentLog.setCreatedBy(dto.getCreatedBy());
        adjustmentLog.setComments(dto.getComments());
        return adjustmentLog;
    }

    private void updateEntityFromDto(BufferAdjustmentLogDTO dto, BufferAdjustmentLog adjustmentLog) {
        adjustmentLog.setBufferId(dto.getBufferId());
        adjustmentLog.setProductId(dto.getProductId());
        adjustmentLog.setLocationId(dto.getLocationId());
        adjustmentLog.setAdjustmentType(dto.getAdjustmentType());
        adjustmentLog.setOldBufferDays(dto.getOldBufferDays());
        adjustmentLog.setNewBufferDays(dto.getNewBufferDays());
        adjustmentLog.setOldBufferUnits(dto.getOldBufferUnits());
        adjustmentLog.setNewBufferUnits(dto.getNewBufferUnits());
        adjustmentLog.setChangePercentage(dto.getChangePercentage());
        adjustmentLog.setTriggerReason(dto.getTriggerReason());
        adjustmentLog.setConsecutiveDaysInZone(dto.getConsecutiveDaysInZone());
        adjustmentLog.setZoneWhenTriggered(dto.getZoneWhenTriggered());
        adjustmentLog.setAdcAtAdjustment(dto.getAdcAtAdjustment());
        adjustmentLog.setSystemRecommended(dto.getSystemRecommended());
        adjustmentLog.setRequiresApproval(dto.getRequiresApproval());
        adjustmentLog.setApprovalStatus(dto.getApprovalStatus());
        adjustmentLog.setApprovedBy(dto.getApprovedBy());
        adjustmentLog.setApprovalDate(dto.getApprovalDate());
        adjustmentLog.setCreatedBy(dto.getCreatedBy());
        adjustmentLog.setComments(dto.getComments());
        // Note: We don't update logId and adjustmentDate in update operations
    }
}

/*please my basic CRUD shall work and it should be able to work on some business logics as metioned make it simple and perfect working focus on these codes only which i have given 
 */