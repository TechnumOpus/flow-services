package com.onified.distribute.service.masterdata;

import com.onified.distribute.dto.ProductDTO;
import com.onified.distribute.dto.request.ProductRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductService {

    List<String> getProductIdsByCategory(String category);

    ProductDTO createProduct(ProductDTO productDto);

    ProductDTO updateProduct(String productId, String tenantSku, String supplierSku, ProductRequestDTO productRequestDto);

    ProductDTO updateProduct(String productId, ProductDTO productDto);

    ProductDTO getProductById(String productId);

    ProductDTO getProductByTenantSku(String tenantSku);

    ProductDTO getProductBySupplierSku(String supplierSku);

    Page<ProductDTO> getAllProducts(Pageable pageable);

    Page<ProductDTO> getActiveProducts(Pageable pageable);

    Page<ProductDTO> getProductsByCategory(String category, Pageable pageable);

    Page<ProductDTO> searchProductsByName(String name, Pageable pageable);

    List<ProductDTO> getProductsByIds(List<String> productIds);

    ProductDTO activateProduct(String productId);

    ProductDTO deactivateProduct(String productId);

    void deleteProduct(String productId);

    boolean existsByProductId(String productId);

    boolean existsByTenantSku(String tenantSku);

    boolean existsBySupplierSku(String supplierSku);
}