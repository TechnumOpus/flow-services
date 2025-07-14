package com.onified.distribute.service.consumption;

import com.onified.distribute.dto.DailyConsumptionLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface DailyConsumptionLogService {
    
    DailyConsumptionLogDTO createDailyConsumptionLog(DailyConsumptionLogDTO logDto);
    
    DailyConsumptionLogDTO updateDailyConsumptionLog(String logId, DailyConsumptionLogDTO logDto);
    
    DailyConsumptionLogDTO getDailyConsumptionLogById(String logId);
    
    Page<DailyConsumptionLogDTO> getAllDailyConsumptionLogs(Pageable pageable);
    
    Page<DailyConsumptionLogDTO> getDailyConsumptionLogsByProduct(String productId, Pageable pageable);
    
    Page<DailyConsumptionLogDTO> getDailyConsumptionLogsByLocation(String locationId, Pageable pageable);
    
    Page<DailyConsumptionLogDTO> getDailyConsumptionLogsByProductAndLocation(String productId, String locationId, Pageable pageable);
    
    Page<DailyConsumptionLogDTO> getDailyConsumptionLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    List<DailyConsumptionLogDTO> getRecentConsumption(String productId, String locationId, Integer days);
    
    DailyConsumptionLogDTO getLatestConsumption(String productId, String locationId);
    
    List<DailyConsumptionLogDTO> getConsumptionForPeriod(String productId, String locationId,
                                                        LocalDateTime startDate, LocalDateTime endDate);
    
    Double calculateAverageConsumption(String productId, String locationId, Integer days);
    
    Double calculateTotalConsumption(String productId, String locationId, Integer days);
    
    void deleteOldConsumptionLogs(LocalDateTime cutoffDate);
    
    void deleteDailyConsumptionLog(String logId);
    
    boolean existsForDate(String productId, String locationId, LocalDateTime consumptionDate);
}
