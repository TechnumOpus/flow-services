package com.onified.distribute.service.dbm;

import com.onified.distribute.dto.BufferAdjustmentLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface BufferAdjustmentLogService {
    
    BufferAdjustmentLogDTO createBufferAdjustmentLog(BufferAdjustmentLogDTO logDto);
    
    BufferAdjustmentLogDTO updateBufferAdjustmentLog(String logId, BufferAdjustmentLogDTO logDto);
    
    BufferAdjustmentLogDTO getBufferAdjustmentLogById(String logId);
    
    Page<BufferAdjustmentLogDTO> getAllBufferAdjustmentLogs(Pageable pageable);
    
    Page<BufferAdjustmentLogDTO> getBufferAdjustmentLogsByBuffer(String bufferId, Pageable pageable);
    
    Page<BufferAdjustmentLogDTO> getBufferAdjustmentLogsByProductAndLocation(String productId, String locationId, Pageable pageable);
    
    Page<BufferAdjustmentLogDTO> getBufferAdjustmentLogsByApprovalStatus(String approvalStatus, Pageable pageable);
    
    Page<BufferAdjustmentLogDTO> getBufferAdjustmentLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Page<BufferAdjustmentLogDTO> getBufferAdjustmentLogsByCreatedBy(String createdBy, Pageable pageable);
    
    Page<BufferAdjustmentLogDTO> getBufferAdjustmentLogsByApprovedBy(String approvedBy, Pageable pageable);
    
    BufferAdjustmentLogDTO approveAdjustment(String logId, String approvedBy);
    
    BufferAdjustmentLogDTO rejectAdjustment(String logId, String approvedBy);
    
    Page<BufferAdjustmentLogDTO> getPendingApprovals(Pageable pageable);
    
    BufferAdjustmentLogDTO logBufferAdjustment(String bufferId, String adjustmentType, Integer oldBufferDays,
                                              Integer newBufferDays, String triggerReason, String createdBy);
    
    void deleteBufferAdjustmentLog(String logId);
}
