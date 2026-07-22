package com.jiubuntu.wms.biz.product.application.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductCreateCommand {

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

}
