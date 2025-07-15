package com.onified.distribute.dto.response;

import com.onified.distribute.dto.ConsumptionProfileDTO;
import com.onified.distribute.dto.DailyConsumptionLogDTO;
import com.onified.distribute.dto.InventoryBufferDTO;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class BufferConsumptionResponseDTO {
    private List<InventoryBufferDTO> inventoryBuffers;
    private List<ConsumptionProfileDTO> consumptionProfiles;
    private Page<DailyConsumptionLogDTO> dailyConsumptionLogs;
}
