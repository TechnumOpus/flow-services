package com.onified.distribute.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDTO {

    private String tenantSku;

    private String supplierSku;

    private String supplierName;

    private String category;

    private String subCategory;

    private String uom;
}