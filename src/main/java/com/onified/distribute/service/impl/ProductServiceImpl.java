package com.onified.distribute.service.impl;

import com.onified.distribute.dto.ProductDTO;
import com.onified.distribute.entity.Product;
import com.onified.distribute.repository.ProductRepository;
import com.onified.distribute.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductDTO createProduct(ProductDTO productDto) {
        log.info("Creating product with SKU: {}", productDto.getSkuCode());
        
        // Validate unique constraints
        if (productRepository.existsByProductId(productDto.getProductId())) {
            throw new IllegalArgumentException("Product ID already exists: " + productDto.getProductId());
        }
        
        if (productRepository.existsBySkuCode(productDto.getSkuCode())) {
            throw new IllegalArgumentException("SKU Code already exists: " + productDto.getSkuCode());
        }
        
        Product product = mapToEntity(productDto);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        if (product.getProductId() == null) {
            product.setProductId("PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getProductId());
        
        return mapToDto(savedProduct);
    }

    @Override
    public ProductDTO updateProduct(String productId, ProductDTO productDto) {
        log.info("Updating product: {}", productId);
        
        Product existingProduct = productRepository.findByProductId(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        
        // Check SKU uniqueness if changed
        if (!existingProduct.getSkuCode().equals(productDto.getSkuCode()) && 
            productRepository.existsBySkuCode(productDto.getSkuCode())) {
            throw new IllegalArgumentException("SKU Code already exists: " + productDto.getSkuCode());
        }
        
        updateEntityFromDto(existingProduct, productDto);
        existingProduct.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully: {}", productId);
        
        return mapToDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(String productId) {
        Product product = productRepository.findByProductId(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        return mapToDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductBySkuCode(String skuCode) {
        Product product = productRepository.findBySkuCode(skuCode)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + skuCode));
        return mapToDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getActiveProducts(Pageable pageable) {
        return productRepository.findByIsActive(true, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByIds(List<String> productIds) {
        return productRepository.findByProductIdIn(productIds)
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Override
    public ProductDTO activateProduct(String productId) {
        log.info("Activating product: {}", productId);
        
        Product product = productRepository.findByProductId(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        
        product.setIsActive(true);
        product.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        log.info("Product activated successfully: {}", productId);
        
        return mapToDto(savedProduct);
    }

    @Override
    public ProductDTO deactivateProduct(String productId) {
        log.info("Deactivating product: {}", productId);
        
        Product product = productRepository.findByProductId(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        log.info("Product deactivated successfully: {}", productId);
        
        return mapToDto(savedProduct);
    }

    @Override
    public void deleteProduct(String productId) {
        log.info("Deleting product: {}", productId);
        
        Product product = productRepository.findByProductId(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        
        productRepository.delete(product);
        log.info("Product deleted successfully: {}", productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProductId(String productId) {
        return productRepository.existsByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySkuCode(String skuCode) {
        return productRepository.existsBySkuCode(skuCode);
    }

    private Product mapToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setProductId(dto.getProductId());
        product.setSkuCode(dto.getSkuCode());
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setSubcategory(dto.getSubcategory());
        product.setUom(dto.getUom());
        product.setMoq(dto.getMoq());
        product.setUnitCost(dto.getUnitCost());
        product.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        product.setCreatedBy(dto.getCreatedBy());
        product.setUpdatedBy(dto.getUpdatedBy());
        return product;
    }

    private ProductDTO mapToDto(Product entity) {
        ProductDTO dto = new ProductDTO();
        dto.setId(entity.getId());
        dto.setProductId(entity.getProductId());
        dto.setSkuCode(entity.getSkuCode());
        dto.setName(entity.getName());
        dto.setCategory(entity.getCategory());
        dto.setSubcategory(entity.getSubcategory());
        dto.setUom(entity.getUom());
        dto.setMoq(entity.getMoq());
        dto.setUnitCost(entity.getUnitCost());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    private void updateEntityFromDto(Product entity, ProductDTO dto) {
        entity.setSkuCode(dto.getSkuCode());
        entity.setName(dto.getName());
        entity.setCategory(dto.getCategory());
        entity.setSubcategory(dto.getSubcategory());
        entity.setUom(dto.getUom());
        entity.setMoq(dto.getMoq());
        entity.setUnitCost(dto.getUnitCost());
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
        entity.setUpdatedBy(dto.getUpdatedBy());
    }
}
