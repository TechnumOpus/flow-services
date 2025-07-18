package com.onified.distribute.scheduler;

import com.onified.distribute.service.sap.SapIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledSyncService {

    private final SapIntegrationService sapIntegrationService;

    @Scheduled(cron = "0 50 18 * * ?") // Daily at 1 AM
    public void scheduledDailySync() {
        log.info("Starting scheduled daily SAP sync");

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            sapIntegrationService.syncDailyConsumptionData(yesterday, yesterday);
            log.info("Scheduled daily SAP sync completed successfully for date: {}", yesterday);
        } catch (Exception e) {
            log.error("Error in scheduled daily SAP sync", e);
        }
    }
}