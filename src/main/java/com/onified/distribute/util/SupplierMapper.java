package com.onified.distribute.util;

import com.onified.distribute.dto.request.SupplierCreateRequest;
import com.onified.distribute.dto.request.SupplierUpdateRequest;
import com.onified.distribute.dto.response.SupplierResponse;
import com.onified.distribute.entity.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public Supplier toEntity(SupplierCreateRequest request) {
        Supplier supplier = new Supplier();
        supplier.setTenantVendorCode(request.getTenantVendorCode());
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactEmail(request.getContactEmail());
        supplier.setPhoneNumber(request.getPhoneNumber());
        return supplier;
    }

    public SupplierResponse toResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        response.setSupplierId(supplier.getSupplierId());
        response.setTenantVendorCode(supplier.getTenantVendorCode());
        response.setSupplierName(supplier.getSupplierName());
        response.setContactEmail(supplier.getContactEmail());
        response.setPhoneNumber(supplier.getPhoneNumber());
        response.setCreatedAt(supplier.getCreatedAt());
        response.setUpdatedAt(supplier.getUpdatedAt());
        return response;
    }

    public void updateEntityFromRequest(Supplier supplier, SupplierUpdateRequest request) {
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactEmail(request.getContactEmail());
        supplier.setPhoneNumber(request.getPhoneNumber());
    }
}