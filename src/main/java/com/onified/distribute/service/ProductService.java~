package com.onified.distribute.service;

import com.onified.distribute.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDto);
    ProductDTO updateProduct(String productId, ProductDTO productDto);
    ProductDTO getProductById(String productId);
    ProductDTO getProductBySkuCode(String skuCode);
    Page<ProductDTO> getAllProducts(Pageable pageable);
    Page<ProductDTO> getActiveProducts(Pageable pageable);
    Page<ProductDTO> getProductsByCategory(String category, Pageable pageable);
    List<ProductDTO> getProductsByIds(List<String> productIds);
    ProductDTO activateProduct(String productId);
    ProductDTO deactivateProduct(String productId);
    void deleteProduct(String productId);
    boolean existsByProductId(String productId);
    boolean existsBySkuCode(String skuCode);
}
