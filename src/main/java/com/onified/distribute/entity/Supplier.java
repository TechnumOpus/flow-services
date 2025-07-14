package com.onified.distribute.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "suppliers")
public class Supplier {

    @Id
    private String supplierId;

    @NotBlank(message = "Tenant vendor code is required")
    @Indexed
    private String tenantVendorCode;

    @NotBlank(message = "Supplier name is required")
    private String supplierName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Contact email is required")
    private String contactEmail;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime updatedAt;
}