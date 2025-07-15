package com.onified.distribute.service.impl.masterdata;

import com.onified.distribute.dto.ProductAttributeDTO;
import com.onified.distribute.entity.ProductAttribute;
import com.onified.distribute.repository.ProductAttributeRepository;
import com.onified.distribute.service.masterdata.ProductAttributeService;
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
public class ProductAttributeServiceImpl implements ProductAttributeService {

    private final ProductAttributeRepository productAttributeRepository;

    @Override
    public ProductAttributeDTO createProductAttribute(ProductAttributeDTO attributeDto) {
        log.info("Creating product attribute: {} for product: {}}",
                attributeDto.getAttributeName(), attributeDto.getProductId());
        
        if (attributeDto.getAttributeId() != null && 
            productAttributeRepository.existsByAttributeId(attributeDto.getAttributeId())) {
            throw new IllegalArgumentException("Attribute ID already exists: " + attributeDto.getAttributeId());
        }
        
        ProductAttribute attribute = mapToEntity(attributeDto);
        attribute.setCreatedAt(LocalDateTime.now());
        attribute.setUpdatedAt(LocalDateTime.now());
        
        if (attribute.getAttributeId() == null) {
            attribute.setAttributeId("ATTR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        ProductAttribute savedAttribute = productAttributeRepository.save(attribute);
        log.info("Product attribute created successfully with ID: {}", savedAttribute.getAttributeId());
        
        return mapToDto(savedAttribute);
    }

    @Override
    public ProductAttributeDTO updateProductAttribute(String attributeId, ProductAttributeDTO attributeDto) {
        log.info("Updating product attribute: {}", attributeId);
        
        ProductAttribute existingAttribute = productAttributeRepository.findByAttributeId(attributeId)
            .orElseThrow(() -> new IllegalArgumentException("Product attribute not found: " + attributeId));
        
        updateEntityFromDto(existingAttribute, attributeDto);
        existingAttribute.setUpdatedAt(LocalDateTime.now());
        
        ProductAttribute savedAttribute = productAttributeRepository.save(existingAttribute);
        log.info("Product attribute updated successfully: {}", attributeId);
        
        return mapToDto(savedAttribute);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAttributeDTO getProductAttributeById(String attributeId) {
        ProductAttribute attribute = productAttributeRepository.findByAttributeId(attributeId)
            .orElseThrow(() -> new IllegalArgumentException("Product attribute not found: " + attributeId));
        return mapToDto(attribute);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeDTO> getAllProductAttributes(Pageable pageable) {
        return productAttributeRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeDTO> getProductAttributesByProduct(String productId, Pageable pageable) {
        return productAttributeRepository.findByProductId(productId, pageable).map(this::mapToDto);
    }



    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeDTO> getProductAttributesByName(String attributeName, Pageable pageable) {
        return productAttributeRepository.findByAttributeName(attributeName, pageable).map(this::mapToDto);
    }


    @Override
    public void deleteProductAttribute(String attributeId) {
        log.info("Deleting product attribute: {}", attributeId);
        
        ProductAttribute attribute = productAttributeRepository.findByAttributeId(attributeId)
            .orElseThrow(() -> new IllegalArgumentException("Product attribute not found: " + attributeId));
        
        productAttributeRepository.delete(attribute);
        log.info("Product attribute deleted successfully: {}", attributeId);
    }

    @Override
    public void deleteProductAttributesByProduct(String productId) {
        log.info("Deleting all product attributes for product: {}", productId);
        
        List<ProductAttribute> attributes = productAttributeRepository.findByProductId(productId, Pageable.unpaged()).getContent();
        productAttributeRepository.deleteAll(attributes);
        
        log.info("Deleted {} product attributes for product: {}", attributes.size(), productId);
    }

    private ProductAttribute mapToEntity(ProductAttributeDTO dto) {
        ProductAttribute attribute = new ProductAttribute();
        attribute.setAttributeId(dto.getAttributeId());
        attribute.setProductId(dto.getProductId());
        attribute.setAttributeName(dto.getAttributeName());
        attribute.setAttributeValue(dto.getAttributeValue());
        attribute.setCreatedBy(dto.getCreatedBy());
        attribute.setUpdatedBy(dto.getUpdatedBy());
        return attribute;
    }

    private ProductAttributeDTO mapToDto(ProductAttribute entity) {
        ProductAttributeDTO dto = new ProductAttributeDTO();
        dto.setId(entity.getId());
        dto.setAttributeId(entity.getAttributeId());
        dto.setProductId(entity.getProductId());
        dto.setAttributeName(entity.getAttributeName());
        dto.setAttributeValue(entity.getAttributeValue());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    private void updateEntityFromDto(ProductAttribute entity, ProductAttributeDTO dto) {
        entity.setProductId(dto.getProductId());
        entity.setAttributeName(dto.getAttributeName());
        entity.setAttributeValue(dto.getAttributeValue());

        entity.setUpdatedBy(dto.getUpdatedBy());
    }
}
