package com.jiubuntu.wms.biz.productunit.ui.payload.response;

import com.jiubuntu.wms.biz.productunit.application.dto.result.ProductUnitResult;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductUnitResponse {

    private final Long id;
    private final Long companyId;
    private final String name;
    private final LocalDateTime createdAt;

    private ProductUnitResponse(Long id, Long companyId, String name, LocalDateTime createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static ProductUnitResponse from(ProductUnitResult result) {
        return new ProductUnitResponse(result.getId(), result.getCompanyId(), result.getName(), result.getCreatedAt());
    }

    public static ProductUnitResponse from(ProductUnit productUnit) {
        return new ProductUnitResponse(
                productUnit.getId(),
                productUnit.getCompany() != null ? productUnit.getCompany().getId() : null,
                productUnit.getName(),
                productUnit.getCreatedAt()
        );
    }

}
