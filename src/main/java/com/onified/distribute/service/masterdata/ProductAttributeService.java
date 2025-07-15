package com.onified.distribute.service.masterdata;

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
    Page<ProductAttributeDTO> getProductAttributesByName(String attributeName, Pageable pageable);
    void deleteProductAttribute(String attributeId);
    void deleteProductAttributesByProduct(String productId);
}
