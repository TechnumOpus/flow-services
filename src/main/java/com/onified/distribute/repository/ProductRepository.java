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

    List<Product> findBySupplierSku(String supplierSku);

    boolean existsByProductId(String productId);

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

    List<Product> findByTenantSku(String tenantSku);

//    List<Product> findByIsActiveTrue();

    @Query(value = "{'category': ?0, 'isActive': true}")
    Page<Product> findActiveByCategoryIgnoreCase(String category, Pageable pageable);

    @Query(value = "{'productId': ?0, 'tenantSku': ?1, 'supplierSku': ?2}")
    Optional<Product> findByProductIdAndTenantSkuAndSupplierSku(
            String productId, String tenantSku, String supplierSku);

    @Query(value = "{'tenantSku': ?0, 'productId': {$ne: ?1}}", exists = true)
    boolean existsByTenantSkuAndProductIdNot(String tenantSku, String productId);

    @Query(value = "{'supplierSku': ?0, 'productId': {$ne: ?1}}", exists = true)
    boolean existsBySupplierSkuAndProductIdNot(String supplierSku, String productId);

    @Query(value = "{'productId': ?0, 'supplierName': ?1, 'tenantSku': ?2, 'supplierSku': ?3}")
    Optional<Product> findByProductIdAndSupplierNameAndTenantSkuAndSupplierSku(
            String productId, String supplierName, String tenantSku, String supplierSku);
}
