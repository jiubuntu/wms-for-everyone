package com.jiubuntu.wms.biz.inventory.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryProductSummaryRow {

    private final String productName;
    private final String skuCode;
    private final int quantity;
    private final int reservedQuantity;

}
