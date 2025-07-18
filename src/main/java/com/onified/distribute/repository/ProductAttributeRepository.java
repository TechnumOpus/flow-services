package com.onified.distribute.repository;

import com.onified.distribute.entity.ProductAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductAttributeRepository extends MongoRepository<ProductAttribute, String> {
    
    Optional<ProductAttribute> findByAttributeId(String attributeId);
    
    boolean existsByAttributeId(String attributeId);
    
    Page<ProductAttribute> findByProductId(String productId, Pageable pageable);

    List<ProductAttribute> findByProductId(String productId);
    
    Page<ProductAttribute> findByAttributeName(String attributeName, Pageable pageable);

}
