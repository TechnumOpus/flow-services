package com.onified.distribute.service;

import com.onified.distribute.dto.ProductAttributeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductAttributeService {
    ProductAttributeDTO createProductAttribute(ProductAttributeDTO attributeDto);
    ProductAttributeDTO updateProductAttribute(String attributeId, ProductAttributeDTO attributeDto);
    ProductAttributeDTO getProductAttributeById(String attributeId);
    Page<ProductAttributeDTO> getAllProductAttributes(Pageable pageable);
    Page<ProductAttributeDTO> getProductAttributesByProduct(String productId, Pageable pageable);
    Page<ProductAttributeDTO> getProductAttributesByLocation(String locationId, Pageable pageable);
    List<ProductAttributeDTO> getProductAttributesByProductAndLocation(String productId, String locationId);
    Page<ProductAttributeDTO> getProductAttributesByName(String attributeName, Pageable pageable);
    String getProductAttributeValue(String productId, String locationId, String attributeName);
    void deleteProductAttribute(String attributeId);
    void deleteProductAttributesByProduct(String productId);
}
