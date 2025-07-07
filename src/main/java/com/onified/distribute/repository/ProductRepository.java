package com.onified.distribute.repository;

import com.onified.distribute.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    
    Optional<Product> findByProductId(String productId);
    
    Optional<Product> findBySkuCode(String skuCode);
    
    boolean existsByProductId(String productId);
    
    boolean existsBySkuCode(String skuCode);
    
    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<Product> findByCategory(String category, Pageable pageable);
    

    List<Product> findByProductIdIn(List<String> productIds);

    @Query(value = "{'category': ?0, 'isActive': true}", fields = "{'productId': 1}")
    List<Product> findProductIdsByCategory(String category);
    
    }
