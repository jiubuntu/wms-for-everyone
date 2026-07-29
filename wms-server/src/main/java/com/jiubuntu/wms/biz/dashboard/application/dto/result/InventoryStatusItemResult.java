package com.jiubuntu.wms.biz.dashboard.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryStatusItemResult {

    private final String productName;
    private final String skuCode;
    private final int quantity;
    private final int availableQuantity;
    private final InventoryHealthStatus status;

}
