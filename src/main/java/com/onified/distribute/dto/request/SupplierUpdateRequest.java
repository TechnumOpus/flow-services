package com.onified.distribute.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierUpdateRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 100, message = "Supplier name must not exceed 100 characters")
    private String supplierName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Contact email is required")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
}