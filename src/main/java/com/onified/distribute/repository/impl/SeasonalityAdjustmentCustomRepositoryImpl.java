package com.onified.distribute.repository.impl;

import com.onified.distribute.entity.SeasonalityAdjustment;
import com.onified.distribute.repository.SeasonalityAdjustmentCustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SeasonalityAdjustmentCustomRepositoryImpl implements SeasonalityAdjustmentCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<SeasonalityAdjustment> findSeasonalityMatrixData(String type, String locationId, String category,
                                                                 String productId, Integer year, Boolean isActive) {
        Query query = new Query();

        // Add type filter
        if (type != null && !type.equals("ALL")) {
            query.addCriteria(Criteria.where("type").is(type));
        }

        // Add locationId filter
        if (locationId != null) {
            query.addCriteria(Criteria.where("locationId").is(locationId));
        }

        // Add category filter
        if (category != null) {
            query.addCriteria(Criteria.where("category").is(category));
        }

        // Add productId filter
        if (productId != null) {
            query.addCriteria(Criteria.where("productId").is(productId));
        }

        // Add year filter
        if (year != null) {
            query.addCriteria(Criteria.where("year").is(year));
        }

        // Add isActive filter
        if (isActive != null) {
            query.addCriteria(Criteria.where("isActive").is(isActive));
        }

        return mongoTemplate.find(query, SeasonalityAdjustment.class);
    }
}
