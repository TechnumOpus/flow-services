package com.onified.distribute.scheduler;

import com.onified.distribute.service.ReplenishmentQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "replenishment.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ReplenishmentQueueScheduler {

    private final ReplenishmentQueueService replenishmentQueueService;

    // Run daily at 6:00 AM
    @Scheduled(cron = "0 0 6 * * ?")
    public void generateDailyReplenishmentQueue() {
        log.info("Starting scheduled daily replenishment queue generation");

        try {
            replenishmentQueueService.generateDailyReplenishmentQueue();
            log.info("Scheduled daily replenishment queue generation completed successfully");
        } catch (Exception e) {
            log.error("Error in scheduled daily replenishment queue generation", e);
        }
    }
}
