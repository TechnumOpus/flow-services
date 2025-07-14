package com.onified.distribute.controller;

import com.onified.distribute.dto.ProductAttributeDTO;
import com.onified.distribute.service.masterdata.ProductAttributeService;
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
@RequestMapping("/api/v1/product-attributes")
@RequiredArgsConstructor
@Validated
public class ProductAttributeController {

    private final ProductAttributeService productAttributeService;

    @PostMapping
    public ResponseEntity<ProductAttributeDTO> createProductAttribute(@Valid @RequestBody ProductAttributeDTO attributeDto) {
        log.info("Creating product attribute: {} for product: {} at location: {}", 
                attributeDto.getAttributeName(), attributeDto.getProductId(), attributeDto.getLocationId());
        ProductAttributeDTO createdAttribute = productAttributeService.createProductAttribute(attributeDto);
        return new ResponseEntity<>(createdAttribute, HttpStatus.CREATED);
    }

    @PutMapping("/{attributeId}")
    public ResponseEntity<ProductAttributeDTO> updateProductAttribute(
            @PathVariable String attributeId,
            @Valid @RequestBody ProductAttributeDTO attributeDto) {
        log.info("Updating product attribute: {}", attributeId);
        ProductAttributeDTO updatedAttribute = productAttributeService.updateProductAttribute(attributeId, attributeDto);
        return ResponseEntity.ok(updatedAttribute);
    }

    @GetMapping("/{attributeId}")
    public ResponseEntity<ProductAttributeDTO> getProductAttributeById(@PathVariable String attributeId) {
        log.info("Fetching product attribute: {}", attributeId);
        ProductAttributeDTO  attribute = productAttributeService.getProductAttributeById(attributeId);
        return ResponseEntity.ok(attribute);
    }

    @GetMapping
    public ResponseEntity<Page<ProductAttributeDTO>> getAllProductAttributes(Pageable pageable) {
        log.info("Fetching all product attributes with pagination");
        Page<ProductAttributeDTO> attributes = productAttributeService.getAllProductAttributes(pageable);
        return ResponseEntity.ok(attributes);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ProductAttributeDTO>> getProductAttributesByProduct(
            @PathVariable String productId, 
            Pageable pageable) {
        log.info("Fetching product attributes by product: {}", productId);
        Page<ProductAttributeDTO  > attributes = productAttributeService.getProductAttributesByProduct(productId, pageable);
        return ResponseEntity.ok(attributes);
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<Page<ProductAttributeDTO>> getProductAttributesByLocation(
            @PathVariable String locationId,
            Pageable pageable) {
        log.info("Fetching product attributes by location: {}", locationId);
        Page<ProductAttributeDTO> attributes = productAttributeService.getProductAttributesByLocation(locationId, pageable);
        return ResponseEntity.ok(attributes);
    }

    @GetMapping("/product/{productId}/location/{locationId}")
    public ResponseEntity<List<ProductAttributeDTO>> getProductAttributesByProductAndLocation(
            @PathVariable String productId,
            @PathVariable String locationId) {
        log.info("Fetching product attributes for product: {} at location: {}", productId, locationId);
        List<ProductAttributeDTO> attributes = productAttributeService.getProductAttributesByProductAndLocation(productId, locationId);
        return ResponseEntity.ok(attributes);
    }

    @GetMapping("/name/{attributeName}")
    public ResponseEntity<Page<ProductAttributeDTO>> getProductAttributesByName(
            @PathVariable String attributeName,
            Pageable pageable) {
        log.info("Fetching product attributes by name: {}", attributeName);
        Page<ProductAttributeDTO> attributes = productAttributeService.getProductAttributesByName(attributeName, pageable);
        return ResponseEntity.ok(attributes);
    }

    @GetMapping("/product/{productId}/location/{locationId}/attribute/{attributeName}/value")
    public ResponseEntity<String> getProductAttributeValue(
            @PathVariable String productId,
            @PathVariable String locationId,
            @PathVariable String attributeName) {
        log.info("Fetching attribute value for product: {} at location: {} for attribute: {}", 
                productId, locationId, attributeName);
        String value = productAttributeService.getProductAttributeValue(productId, locationId, attributeName);
        return ResponseEntity.ok(value);
    }

    @DeleteMapping("/{attributeId}")
    public ResponseEntity<Void> deleteProductAttribute(@PathVariable String attributeId) {
        log.info("Deleting product attribute: {}", attributeId);
        productAttributeService.deleteProductAttribute(attributeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Void> deleteProductAttributesByProduct(@PathVariable String productId) {
        log.info("Deleting all product attributes for product: {}", productId);
        productAttributeService.deleteProductAttributesByProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
