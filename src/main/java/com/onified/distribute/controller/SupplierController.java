package com.onified.distribute.controller;

import com.onified.distribute.dto.request.SupplierCreateRequest;
import com.onified.distribute.dto.request.SupplierUpdateRequest;
import com.onified.distribute.dto.response.SupplierResponse;
import com.onified.distribute.service.masterdata.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Slf4j
@Validated
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody SupplierCreateRequest request) {
        log.info("REST request to create supplier: {}", request.getTenantVendorCode());

        try {
            SupplierResponse response = supplierService.createSupplier(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Error creating supplier: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Unexpected error creating supplier: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable String id) {
        log.info("REST request to get supplier by ID: {}", id);

        try {
            SupplierResponse response = supplierService.getSupplierById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Supplier not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Unexpected error getting supplier: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers(
            @RequestParam(required = false) String supplierName) {
        log.info("REST request to get all suppliers with filter - supplierName: {}", supplierName);

        try {
            List<SupplierResponse> responses = supplierService.getAllSuppliers(supplierName);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Unexpected error getting all suppliers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(@PathVariable String id,
                                                           @Valid @RequestBody SupplierUpdateRequest request) {
        log.info("REST request to update supplier with ID: {}", id);

        try {
            SupplierResponse response = supplierService.updateSupplier(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error updating supplier: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Unexpected error updating supplier: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable String id) {
        log.info("REST request to delete supplier with ID: {}", id);

        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Supplier not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Unexpected error deleting supplier: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<SupplierResponse>> searchSuppliersByName(@RequestParam String name) {
        log.info("REST request to search suppliers by name: {}", name);

        try {
            List<SupplierResponse> responses = supplierService.searchSuppliersByName(name);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Unexpected error searching suppliers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tenant-vendor-code/{code}")
    public ResponseEntity<SupplierResponse> getSupplierByTenantVendorCode(@PathVariable String code) {
        log.info("REST request to get supplier by tenant vendor code: {}", code);

        try {
            SupplierResponse response = supplierService.getSupplierByTenantVendorCode(code);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Supplier not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Unexpected error getting supplier: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}