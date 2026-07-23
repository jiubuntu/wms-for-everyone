package com.jiubuntu.wms.biz.inventory.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class InventoryResult {

    private final Long id;
    private final Long warehouseId;
    private final Long locationId;
    private final String locationCode;
    private final Long productId;
    private final String productSkuCode;
    private final String productName;
    private final String unitName;
    private final String lotNumber;
    private final LocalDate manufactureDate;
    private final LocalDate expiryDate;
    private final Integer quantity;
    private final Integer reservedQuantity;

}
