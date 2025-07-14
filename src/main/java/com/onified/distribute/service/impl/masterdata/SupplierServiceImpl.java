package com.onified.distribute.service.impl.masterdata;

import com.onified.distribute.dto.request.SupplierCreateRequest;
import com.onified.distribute.dto.request.SupplierUpdateRequest;
import com.onified.distribute.dto.response.SupplierResponse;
import com.onified.distribute.entity.Supplier;
import com.onified.distribute.repository.SupplierRepository;
import com.onified.distribute.service.masterdata.SupplierService;
import com.onified.distribute.util.SupplierMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    public SupplierResponse createSupplier(SupplierCreateRequest request) {
        log.info("Creating supplier with tenant vendor code: {}", request.getTenantVendorCode());

        // Check if supplier already exists
        if (supplierRepository.existsByTenantVendorCode(request.getTenantVendorCode())) {
            throw new IllegalArgumentException("Supplier with tenant vendor code already exists: "
                    + request.getTenantVendorCode());
        }

        if (supplierRepository.existsByContactEmail(request.getContactEmail())) {
            throw new IllegalArgumentException("Supplier with contact email already exists: "
                    + request.getContactEmail());
        }

        Supplier supplier = supplierMapper.toEntity(request);
        supplier.setCreatedAt(LocalDateTime.now());
        supplier.setUpdatedAt(LocalDateTime.now());

        Supplier savedSupplier = supplierRepository.save(supplier);
        log.info("Supplier created successfully with ID: {}", savedSupplier.getSupplierId());

        return supplierMapper.toResponse(savedSupplier);
    }

    @Override
    public SupplierResponse getSupplierById(String supplierId) {
        log.info("Fetching supplier with ID: {}", supplierId);

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + supplierId));

        return supplierMapper.toResponse(supplier);
    }

    @Override
    public List<SupplierResponse> getAllSuppliers() {
        log.info("Fetching all suppliers");

        List<Supplier> suppliers = supplierRepository.findAll();
        return suppliers.stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierResponse> getAllSuppliers(String supplierName) {
        log.info("Fetching all suppliers with filter - supplierName: {}", supplierName);

        List<Supplier> suppliers;
        if (supplierName != null && !supplierName.trim().isEmpty()) {
            suppliers = supplierRepository.findBySupplierNameContainingIgnoreCase(supplierName.trim());
        } else {
            suppliers = supplierRepository.findAll();
        }

        return suppliers.stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SupplierResponse updateSupplier(String supplierId, SupplierUpdateRequest request) {
        log.info("Updating supplier with ID: {}", supplierId);

        Supplier existingSupplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + supplierId));

        // Check if email is being changed and if new email already exists
        if (!existingSupplier.getContactEmail().equals(request.getContactEmail())
                && supplierRepository.existsByContactEmail(request.getContactEmail())) {
            throw new IllegalArgumentException("Supplier with contact email already exists: "
                    + request.getContactEmail());
        }

        supplierMapper.updateEntityFromRequest(existingSupplier, request);
        existingSupplier.setUpdatedAt(LocalDateTime.now());

        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        log.info("Supplier updated successfully with ID: {}", updatedSupplier.getSupplierId());

        return supplierMapper.toResponse(updatedSupplier);
    }

    @Override
    public void deleteSupplier(String supplierId) {
        log.info("Deleting supplier with ID: {}", supplierId);

        if (!supplierRepository.existsById(supplierId)) {
            throw new IllegalArgumentException("Supplier not found with ID: " + supplierId);
        }

        supplierRepository.deleteById(supplierId);
        log.info("Supplier deleted successfully with ID: {}", supplierId);
    }

    @Override
    public SupplierResponse getSupplierByTenantVendorCode(String tenantVendorCode) {
        log.info("Fetching supplier with tenant vendor code: {}", tenantVendorCode);

        Supplier supplier = supplierRepository.findByTenantVendorCode(tenantVendorCode)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with tenant vendor code: "
                        + tenantVendorCode));

        return supplierMapper.toResponse(supplier);
    }

    @Override
    public List<SupplierResponse> searchSuppliersByName(String supplierName) {
        log.info("Searching suppliers with name containing: {}", supplierName);

        List<Supplier> suppliers = supplierRepository.findBySupplierNameContainingIgnoreCase(supplierName);
        return suppliers.stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }
}