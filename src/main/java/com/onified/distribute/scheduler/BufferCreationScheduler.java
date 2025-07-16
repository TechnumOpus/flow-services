//package com.onified.distribute.scheduler;
//
//import com.onified.distribute.dto.InventoryBufferDTO;
//import com.onified.distribute.dto.ReviewCycleDTO;
//import com.onified.distribute.entity.ConsumptionProfile;
//import com.onified.distribute.entity.DailyConsumptionLog;
//import com.onified.distribute.entity.LeadTime;
//import com.onified.distribute.entity.ReviewCycle;
//import com.onified.distribute.repository.ConsumptionProfileRepository;
//import com.onified.distribute.repository.DailyConsumptionLogRepository;
//import com.onified.distribute.repository.LeadTimeRepository;
//import com.onified.distribute.repository.ReviewCycleRepository;
//import com.onified.distribute.service.dbm.InventoryBufferService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class BufferCreationScheduler {
//
//    private final InventoryBufferService inventoryBufferService;
//    private final LeadTimeRepository leadTimeRepository;
//    private final ConsumptionProfileRepository consumptionProfileRepository;
//    private final DailyConsumptionLogRepository dailyConsumptionLogRepository;
//    private final ReviewCycleRepository reviewCycleRepository;
//
//    @Scheduled(cron = "0 * * * * * ?")
//    @Transactional
//    public void createAutomaticBuffers() {
//        log.info("Starting automatic buffer creation process at {}", LocalDateTime.now());
//        List<LeadTime> activeLeadTimes = leadTimeRepository.findByIsActiveTrue();
//
//        for (LeadTime leadTime : activeLeadTimes) {
//            try {
//                processBufferForLeadTime(leadTime);
//            } catch (Exception e) {
//                log.error("Error processing buffer for product: {} and location: {}. Error: {}",
//                        leadTime.getProductId(), leadTime.getLocationId(), e.getMessage());
//            }
//        }
//
//        log.info("Completed automatic buffer creation process at {}", LocalDateTime.now());
//    }
//
//    /**
//     * Process buffer creation or update for a specific lead time
//     * This method can be called both from scheduled job and when lead time is created/updated
//     */
//    @Transactional
//    public void processBufferForLeadTime(LeadTime leadTime) {
//        String productId = leadTime.getProductId();
//        String locationId = leadTime.getLocationId();
//
//        log.info("Processing buffer for product: {} and location: {}", productId, locationId);
//
//        ConsumptionProfile consumptionProfile = consumptionProfileRepository
//                .findByProductIdAndLocationId(productId, locationId)
//                .orElse(null);
//
//        if (consumptionProfile == null) {
//            log.warn("No consumption profile found for product: {} and location: {}", productId, locationId);
//            return;
//        }
//
//        // Get yesterday's consumption
//        int yesterdaysConsumption = getYesterdaysConsumption(productId, locationId);
//
//        // Check if buffer already exists
//        if (inventoryBufferService.existsByProductAndLocation(productId, locationId)) {
//            log.info("Buffer exists for product: {} and location: {}. Updating...", productId, locationId);
//            updateExistingBuffer(leadTime, consumptionProfile, yesterdaysConsumption);
//        } else {
//            log.info("Creating new buffer for product: {} and location: {}", productId, locationId);
//            createNewBuffer(leadTime, consumptionProfile, yesterdaysConsumption);
//        }
//    }
//
//    /**
//     * Create a new buffer
//     */
//    private void createNewBuffer(LeadTime leadTime, ConsumptionProfile consumptionProfile, int yesterdaysConsumption) {
//        try {
//            InventoryBufferDTO bufferDto = createBufferDto(leadTime, consumptionProfile, yesterdaysConsumption);
//            InventoryBufferDTO createdBuffer = inventoryBufferService.createInventoryBuffer(bufferDto);
//
//            log.info("Successfully created buffer for product: {} at location: {} with yesterday's consumption: {}",
//                    leadTime.getProductId(), leadTime.getLocationId(), yesterdaysConsumption);
//        } catch (Exception e) {
//            log.error("Failed to create buffer for product: {} at location: {}. Error: {}",
//                    leadTime.getProductId(), leadTime.getLocationId(), e.getMessage());
//            throw e;
//        }
//    }
//
//    /**
//     * Update existing buffer with new lead time data
//     */
//    private void updateExistingBuffer(LeadTime leadTime, ConsumptionProfile consumptionProfile, int yesterdaysConsumption) {
//        try {
//            // Get existing buffer
//            InventoryBufferDTO existingBuffer = inventoryBufferService
//                    .getBufferByProductAndLocation(leadTime.getProductId(), leadTime.getLocationId());
//
//            // Recalculate buffer values based on new lead time
//            InventoryBufferDTO updatedBufferDto = recalculateBufferDto(existingBuffer, leadTime, consumptionProfile, yesterdaysConsumption);
//
//            // Update the buffer
//            InventoryBufferDTO updatedBuffer = inventoryBufferService.updateInventoryBuffer(existingBuffer.getBufferId(), updatedBufferDto);
//
//            log.info("Successfully updated buffer for product: {} at location: {} with new lead time data",
//                    leadTime.getProductId(), leadTime.getLocationId());
//        } catch (Exception e) {
//            log.error("Failed to update buffer for product: {} at location: {}. Error: {}",
//                    leadTime.getProductId(), leadTime.getLocationId(), e.getMessage());
//            throw e;
//        }
//    }
//
//    /**
//     * Recalculate buffer DTO based on new lead time while preserving existing inventory data
//     */
//    private InventoryBufferDTO recalculateBufferDto(InventoryBufferDTO existingBuffer, LeadTime leadTime,
//                                                    ConsumptionProfile consumptionProfile, int yesterdaysConsumption) {
//
//        // Calculate new buffer days and units based on updated lead time
//        double totalLeadTimeDays = calculateTotalLeadTimeDays(leadTime);
//        double dailyConsumption = consumptionProfile.getAdc30d() != null ? consumptionProfile.getAdc30d() : 0.0;
//        int newBufferUnits = (int) Math.ceil(dailyConsumption * totalLeadTimeDays * 1.2); // 20% safety factor
//
//        // Update the existing buffer with new calculations
//        existingBuffer.setBufferDays((int) Math.ceil(totalLeadTimeDays));
//        existingBuffer.setBufferUnits(newBufferUnits);
//        existingBuffer.setUpdatedAt(LocalDateTime.now());
//
//        // Recalculate net available quantity
//        existingBuffer.setNetAvailableQty(existingBuffer.getCurrentInventory() + existingBuffer.getInPipelineQty());
//
//        // Recalculate buffer consumed percentage
//        double bufferConsumedPct = newBufferUnits > 0 ?
//                ((double) (newBufferUnits - existingBuffer.getNetAvailableQty()) / newBufferUnits) * 100 : 0.0;
//        existingBuffer.setBufferConsumedPct(Math.max(0.0, bufferConsumedPct));
//
//        // Determine current zone based on net available quantity
//        String currentZone = determineZone(existingBuffer.getNetAvailableQty(), newBufferUnits);
//        existingBuffer.setCurrentZone(currentZone);
//
//        return existingBuffer;
//    }
//
//    private double calculateTotalLeadTimeDays(LeadTime leadTime) {
//        double totalLeadTimeDays = 0.0;
//        if (leadTime.getOrderLeadTime() != null)
//            totalLeadTimeDays += leadTime.getOrderLeadTime();
//        if (leadTime.getManufacturingTime() != null)
//            totalLeadTimeDays += leadTime.getManufacturingTime();
//        if (leadTime.getTransportTime() != null)
//            totalLeadTimeDays += leadTime.getTransportTime();
//        return totalLeadTimeDays;
//    }
//
//    private int getYesterdaysConsumption(String productId, String locationId) {
//        LocalDate yesterday = LocalDate.now().minusDays(1);
//        LocalDateTime startOfYesterday = yesterday.atStartOfDay();
//        LocalDateTime endOfYesterday = yesterday.plusDays(1).atStartOfDay();
//
//        return dailyConsumptionLogRepository
//                .findByProductIdAndLocationIdAndConsumptionDateBetween(
//                        productId, locationId, startOfYesterday, endOfYesterday)
//                .map(DailyConsumptionLog::getQuantityConsumed)
//                .orElse(0);
//    }
//
//    private InventoryBufferDTO createBufferDto(LeadTime leadTime, ConsumptionProfile consumptionProfile, int yesterdaysConsumption) {
//        InventoryBufferDTO bufferDto = new InventoryBufferDTO();
//        Optional<ReviewCycle> cycle = reviewCycleRepository.findByCycleId("CYCLE001");
//
//        // Set default values
//        bufferDto.setBufferDays(0);
//        bufferDto.setBufferUnits(0);
//        bufferDto.setGreenThresholdPct(33.0);
//        bufferDto.setYellowThresholdPct(33.0);
//        bufferDto.setRedThresholdPct(33.0);
//        bufferDto.setCurrentInventory(0);
//        bufferDto.setInPipelineQty(0);
//        bufferDto.setNetAvailableQty(0);
//        bufferDto.setBufferConsumedPct(0.0);
//        bufferDto.setCurrentZone("GREEN");
//        bufferDto.setDbmReviewPeriodDays(0);
//        bufferDto.setNextReviewDue(null);
//        bufferDto.setConsecutiveZoneDays(0);
//        bufferDto.setAdjustmentThresholdDays(0);
//
//        // Set basic properties
//        bufferDto.setBufferId(UUID.randomUUID().toString());
//        bufferDto.setProductId(leadTime.getProductId());
//        bufferDto.setLocationId(leadTime.getLocationId());
//        bufferDto.setBufferType("SAFETY");
//        bufferDto.setIsActive(true);
//        bufferDto.setCreatedAt(LocalDateTime.now());
//        bufferDto.setUpdatedAt(LocalDateTime.now());
//
//        // Calculate buffer days and units
//        double totalLeadTimeDays = calculateTotalLeadTimeDays(leadTime);
//        bufferDto.setBufferDays((int) Math.ceil(totalLeadTimeDays));
//
//        double dailyConsumption = consumptionProfile.getAdc30d() != null ? consumptionProfile.getAdc30d() : 0.0;
//        int bufferUnits = (int) Math.ceil(dailyConsumption * totalLeadTimeDays * 1.2);
//        bufferDto.setBufferUnits(bufferUnits);
//
//        // Calculate current inventory based on buffer units minus yesterday's consumption
//        int currentInventory = Math.max(0, bufferUnits - yesterdaysConsumption);
//        bufferDto.setCurrentInventory(currentInventory);
//        bufferDto.setNetAvailableQty(currentInventory);
//
//        // Calculate buffer consumed percentage
//        double bufferConsumedPct = bufferUnits > 0 ? ((double) yesterdaysConsumption / bufferUnits) * 100 : 0.0;
//        bufferDto.setBufferConsumedPct(bufferConsumedPct);
//
//        // Determine current zone
//        String currentZone = determineZone(currentInventory, bufferUnits);
//        bufferDto.setCurrentZone(currentZone);
//
//        // Set review cycle information
//        bufferDto.setReviewCycleId("CYCLE001");
//        if (cycle.isPresent()) {
//            int reviewPeriodDays = Math.abs(cycle.get().getStartDay() - cycle.get().getEndDay());
//            bufferDto.setDbmReviewPeriodDays(reviewPeriodDays);
//            bufferDto.setNextReviewDue(LocalDateTime.now().plusDays(reviewPeriodDays));
//        }
//
//        return bufferDto;
//    }
//
//    private String determineZone(int currentInventory, int bufferUnits) {
//        if (bufferUnits == 0) return "GREEN";
//
//        double inventoryPct = ((double) currentInventory / bufferUnits) * 100;
//        if (inventoryPct >= 66.0) return "GREEN";
//        else if (inventoryPct >= 33.0) return "YELLOW";
//        else return "RED";
//    }
//}
