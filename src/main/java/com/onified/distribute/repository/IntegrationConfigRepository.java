package com.onified.distribute.repository;

import com.onified.distribute.entity.IntegrationConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationConfigRepository extends MongoRepository<IntegrationConfig, String> {

    IntegrationConfig findByConfigId(String configId);

    Page<IntegrationConfig> findByIsActiveTrue(Pageable pageable);

}
