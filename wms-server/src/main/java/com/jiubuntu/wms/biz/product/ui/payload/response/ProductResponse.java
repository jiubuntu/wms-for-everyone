package com.jiubuntu.wms.biz.product.ui.payload.response;

import com.jiubuntu.wms.biz.product.application.dto.result.ProductResult;
import com.jiubuntu.wms.biz.product.domain.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ProductResponse {

    private final Long id;
    private final Long companyId;
    private final String skuCode;
    private final String name;
    private final Long categoryId;
    private final Long storageTypeId;
    private final Long baseUnitId;
    private final Long subUnitId;
    private final BigDecimal unitConversionRate;
    private final boolean lotTracking;
    private final String description;
    private final LocalDateTime createdAt;

    private ProductResponse(Long id, Long companyId, String skuCode, String name, Long categoryId,
                             Long storageTypeId, Long baseUnitId, Long subUnitId, BigDecimal unitConversionRate,
                             boolean lotTracking, String description, LocalDateTime createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.skuCode = skuCode;
        this.name = name;
        this.categoryId = categoryId;
        this.storageTypeId = storageTypeId;
        this.baseUnitId = baseUnitId;
        this.subUnitId = subUnitId;
        this.unitConversionRate = unitConversionRate;
        this.lotTracking = lotTracking;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static ProductResponse from(ProductResult result) {
        return new ProductResponse(
                result.getId(),
                result.getCompanyId(),
                result.getSkuCode(),
                result.getName(),
                result.getCategoryId(),
                result.getStorageTypeId(),
                result.getBaseUnitId(),
                result.getSubUnitId(),
                result.getUnitConversionRate(),
                result.isLotTracking(),
                result.getDescription(),
                result.getCreatedAt()
        );
    }

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCompany() != null ? product.getCompany().getId() : null,
                product.getSkuCode(),
                product.getName(),
                product.getCategory().getId(),
                product.getStorageType().getId(),
                product.getBaseUnit().getId(),
                product.getSubUnit() != null ? product.getSubUnit().getId() : null,
                product.getUnitConversionRate(),
                product.isLotTracking(),
                product.getDescription(),
                product.getCreatedAt()
        );
    }

}
