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

    Optional<Product> findBySupplierSku(String supplierSku);

    boolean existsByProductId(String productId);

    boolean existsBySkuCode(String skuCode);

    boolean existsByTenantSku(String tenantSku);

    boolean existsBySupplierSku(String supplierSku);

    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);

    Page<Product> findByCategory(String category, Pageable pageable);


    @Query(value = "{'category': ?0, 'isActive': true}", fields = "{'productId': 1}")
    List<Product> findProductIdsByCategory(String category);

    @Query(value = "{'name': {$regex: ?0, $options: 'i'}}")
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);


    Optional<Product> findByProductId(String productId);
    List<Product> findByProductIdIn(List<String> productIds);
    Optional<Product> findBySkuCode(String skuCode);
    Optional<Product> findByTenantSku(String tenantSku);
    List<Product> findByIsActiveTrue();


    @Query(value = "{'category': ?0, 'isActive': true}")
    Page<Product> findActiveByCategoryIgnoreCase(String category, Pageable pageable);
}
