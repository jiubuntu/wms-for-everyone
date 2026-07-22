package com.jiubuntu.wms.biz.product.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductResult {

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

}
