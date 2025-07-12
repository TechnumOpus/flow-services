package com.onified.distribute.controller;

import com.onified.distribute.dto.BufferAdjustmentLogDTO;
import com.onified.distribute.service.BufferAdjustmentLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/buffer-adjustment-logs")
@RequiredArgsConstructor
@Validated
public class BufferAdjustmentLogController {

    private final BufferAdjustmentLogService bufferAdjustmentLogService;

    @PostMapping
    public ResponseEntity<BufferAdjustmentLogDTO> createBufferAdjustmentLog(@Valid @RequestBody BufferAdjustmentLogDTO logDto) {
        log.info("Creating buffer adjustment log for buffer: {}", logDto.getBufferId());
        
        BufferAdjustmentLogDTO createdLog = bufferAdjustmentLogService.createBufferAdjustmentLog(logDto);
        return new ResponseEntity<>(createdLog, HttpStatus.CREATED);
    }

    @PutMapping("/{logId}")
    public ResponseEntity<BufferAdjustmentLogDTO> updateBufferAdjustmentLog(
            @PathVariable String logId,
            @Valid @RequestBody BufferAdjustmentLogDTO logDto) {
        log.info("Updating buffer adjustment log: {}", logId);
        
        BufferAdjustmentLogDTO updatedLog = bufferAdjustmentLogService.updateBufferAdjustmentLog(logId, logDto);
        return ResponseEntity.ok(updatedLog);
    }

    @GetMapping("/{logId}")
    public ResponseEntity<BufferAdjustmentLogDTO> getBufferAdjustmentLogById(@PathVariable String logId) {
        log.info("Fetching buffer adjustment log: {}", logId);
        
        BufferAdjustmentLogDTO log = bufferAdjustmentLogService.getBufferAdjustmentLogById(logId);
        return ResponseEntity.ok(log);
    }

    @GetMapping
    public ResponseEntity<Page<BufferAdjustmentLogDTO>> getAllBufferAdjustmentLogs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
                        @RequestParam(defaultValue = "adjustmentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Fetching all buffer adjustment logs - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<BufferAdjustmentLogDTO> logs = bufferAdjustmentLogService.getAllBufferAdjustmentLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/buffer/{bufferId}")
    public ResponseEntity<Page<BufferAdjustmentLogDTO>> getBufferAdjustmentLogsByBuffer(
            @PathVariable String bufferId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Fetching buffer adjustment logs by buffer: {}", bufferId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("adjustmentDate").descending());
        Page<BufferAdjustmentLogDTO> logs = bufferAdjustmentLogService.getBufferAdjustmentLogsByBuffer(bufferId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/product/{productId}/location/{locationId}")
    public ResponseEntity<Page<BufferAdjustmentLogDTO>> getBufferAdjustmentLogsByProductAndLocation(
            @PathVariable String productId,
            @PathVariable String locationId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Fetching buffer adjustment logs by product: {} and location: {}", productId, locationId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("adjustmentDate").descending());
        Page<BufferAdjustmentLogDTO> logs = bufferAdjustmentLogService.getBufferAdjustmentLogsByProductAndLocation(
                productId, locationId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/approval-status/{approvalStatus}")
    public ResponseEntity<Page<BufferAdjustmentLogDTO>> getBufferAdjustmentLogsByApprovalStatus(
            @PathVariable String approvalStatus,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Fetching buffer adjustment logs by approval status: {}", approvalStatus);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("adjustmentDate").descending());
        Page<BufferAdjustmentLogDTO> logs = bufferAdjustmentLogService.getBufferAdjustmentLogsByApprovalStatus(
                approvalStatus, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<BufferAdjustmentLogDTO>> getBufferAdjustmentLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Fetching buffer adjustment logs between {} and {}", startDate, endDate);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("adjustmentDate").descending());
        Page<BufferAdjustmentLogDTO> logs = bufferAdjustmentLogService.getBufferAdjustmentLogsByDateRange(
                startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }

//    @PutMapping("/{logId}/approve")
//    public ResponseEntity<BufferAdjustmentLogDTO> approveAdjustment(
//            @PathVariable String logId,
//            @RequestParam String approvedBy) {
//        log.info("Approving buffer adjustment log: {} by: {}", logId, approvedBy);
//
//        BufferAdjustmentLogDTO approvedLog = bufferAdjustmentLogService.approveAdjustment(logId, approvedBy);
//        return ResponseEntity.ok(approvedLog);
//    }

//    @PutMapping("/{logId}/reject")
//    public ResponseEntity<BufferAdjustmentLogDTO> rejectAdjustment(
//            @PathVariable String logId,
//            @RequestParam String approvedBy) {
//        log.info("Rejecting buffer adjustment log: {} by: {}", logId, approvedBy);
//
//        BufferAdjustmentLogDTO rejectedLog = bufferAdjustmentLogService.rejectAdjustment(logId, approvedBy);
//        return ResponseEntity.ok(rejectedLog);
//    }

//    @GetMapping("/pending-approvals")
//    public ResponseEntity<Page<BufferAdjustmentLogDTO>> getPendingApprovals(
//            @RequestParam(defaultValue = "0") @Min(0) int page,
//            @RequestParam(defaultValue = "20") @Min(1) int size) {
//        log.info("Fetching pending buffer adjustment approvals");
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by("adjustmentDate").ascending());
//        Page<BufferAdjustmentLogDTO> logs = bufferAdjustmentLogService.getPendingApprovals(pageable);
//        return ResponseEntity.ok(logs);
//    }

    @PostMapping("/log-adjustment")
    public ResponseEntity<BufferAdjustmentLogDTO> logBufferAdjustment(
            @RequestParam String bufferId,
            @RequestParam String adjustmentType,
            @RequestParam Integer oldBufferDays,
            @RequestParam Integer newBufferDays,
            @RequestParam String triggerReason,
            @RequestParam String createdBy) {
        log.info("Logging buffer adjustment for buffer: {} with type: {}", bufferId, adjustmentType);
        
        BufferAdjustmentLogDTO adjustmentLog = bufferAdjustmentLogService.logBufferAdjustment(
                bufferId, adjustmentType, oldBufferDays, newBufferDays, triggerReason, createdBy);
        return new ResponseEntity<>(adjustmentLog, HttpStatus.CREATED);
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteBufferAdjustmentLog(@PathVariable String logId) {
        log.info("Deleting buffer adjustment log: {}", logId);
        
        bufferAdjustmentLogService.deleteBufferAdjustmentLog(logId);
        return ResponseEntity.noContent().build();
    }
}
