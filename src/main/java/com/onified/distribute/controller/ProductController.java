package com.onified.distribute.controller;

import com.onified.distribute.dto.ProductDTO;
import com.onified.distribute.service.masterdata.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDto) {
        log.info("Creating product with SKU: {}, Tenant SKU: {}, Supplier SKU: {}",
                productDto.getSkuCode(), productDto.getTenantSku(), productDto.getSupplierSku());
        ProductDTO createdProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody ProductDTO productDto) {
        log.info("Updating product: {}", productId);
        ProductDTO updatedProduct = productService.updateProduct(productId, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String productId) {
        log.info("Fetching product: {}", productId);
        ProductDTO product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @PathVariable String category,
            Pageable pageable) {
        log.info("Fetching products by category: {}", category);
        Page<ProductDTO> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/sku/{skuCode}")
    public ResponseEntity<ProductDTO> getProductBySkuCode(@PathVariable String skuCode) {
        log.info("Fetching product by SKU: {}", skuCode);
        ProductDTO product = productService.getProductBySkuCode(skuCode);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/tenant-sku/{tenantSku}")
    public ResponseEntity<ProductDTO> getProductByTenantSku(@PathVariable String tenantSku) {
        log.info("Fetching product by Tenant SKU: {}", tenantSku);
        ProductDTO product = productService.getProductByTenantSku(tenantSku);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/supplier-sku/{supplierSku}")
    public ResponseEntity<ProductDTO> getProductBySupplierSku(@PathVariable String supplierSku) {
        log.info("Fetching product by Supplier SKU: {}", supplierSku);
        ProductDTO product = productService.getProductBySupplierSku(supplierSku);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination");
        Page<ProductDTO> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<ProductDTO>> getActiveProducts(Pageable pageable) {
        log.info("Fetching active products with pagination");
        Page<ProductDTO> products = productService.getActiveProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProductsByName(
            @RequestParam String name,
            Pageable pageable) {
        log.info("Searching products by name: {}", name);
        Page<ProductDTO> products = productService.searchProductsByName(name, pageable);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ProductDTO>> getProductsByIds(@RequestBody List<String> productIds) {
        log.info("Fetching products by IDs: {}", productIds);
        List<ProductDTO> products = productService.getProductsByIds(productIds);
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{productId}/activate")
    public ResponseEntity<ProductDTO> activateProduct(@PathVariable String productId) {
        log.info("Activating product: {}", productId);
        ProductDTO product = productService.activateProduct(productId);
        return ResponseEntity.ok(product);
    }

    @PatchMapping("/{productId}/deactivate")
    public ResponseEntity<ProductDTO> deactivateProduct(@PathVariable String productId) {
        log.info("Deactivating product: {}", productId);
        ProductDTO product = productService.deactivateProduct(productId);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        log.info("Deleting product: {}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/exists")
    public ResponseEntity<Boolean> existsByProductId(@PathVariable String productId) {
        boolean exists = productService.existsByProductId(productId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/sku/{skuCode}/exists")
    public ResponseEntity<Boolean> existsBySkuCode(@PathVariable String skuCode) {
        boolean exists = productService.existsBySkuCode(skuCode);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/tenant-sku/{tenantSku}/exists")
    public ResponseEntity<Boolean> existsByTenantSku(@PathVariable String tenantSku) {
        boolean exists = productService.existsByTenantSku(tenantSku);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/supplier-sku/{supplierSku}/exists")
    public ResponseEntity<Boolean> existsBySupplierSku(@PathVariable String supplierSku) {
        boolean exists = productService.existsBySupplierSku(supplierSku);
        return ResponseEntity.ok(exists);
    }
}
