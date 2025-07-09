package com.onified.distribute.repository;

import com.onified.distribute.entity.SeasonalityAdjustment;
import java.util.List;

public interface SeasonalityAdjustmentCustomRepository {
    List<SeasonalityAdjustment> findSeasonalityMatrixData(String type, String locationId, String category, String productId, Integer year, Boolean isActive);
}
