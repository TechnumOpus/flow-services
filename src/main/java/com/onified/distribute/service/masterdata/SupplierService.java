package com.onified.distribute.service.masterdata;

import com.onified.distribute.dto.request.SupplierCreateRequest;
import com.onified.distribute.dto.request.SupplierUpdateRequest;
import com.onified.distribute.dto.response.SupplierResponse;

import java.util.List;

public interface SupplierService {

    SupplierResponse createSupplier(SupplierCreateRequest request);

    SupplierResponse getSupplierById(String supplierId);

    List<SupplierResponse> getAllSuppliers();

    List<SupplierResponse> getAllSuppliers(String supplierName);

    SupplierResponse updateSupplier(String supplierId, SupplierUpdateRequest request);

    void deleteSupplier(String supplierId);

    SupplierResponse getSupplierByTenantVendorCode(String tenantVendorCode);

    List<SupplierResponse> searchSuppliersByName(String supplierName);
}