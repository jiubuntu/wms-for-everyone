package com.jiubuntu.wms.biz.dashboard.ui.payload.response;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.InventoryHealthStatus;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.InventoryStatusItemResult;
import lombok.Getter;

@Getter
public class InventoryStatusItemResponse {

    private final String productName;
    private final String skuCode;
    private final int quantity;
    private final int availableQuantity;
    private final InventoryHealthStatus status;

    private InventoryStatusItemResponse(String productName, String skuCode, int quantity, int availableQuantity,
                                         InventoryHealthStatus status) {
        this.productName = productName;
        this.skuCode = skuCode;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.status = status;
    }

    public static InventoryStatusItemResponse from(InventoryStatusItemResult result) {
        return new InventoryStatusItemResponse(
                result.getProductName(), result.getSkuCode(), result.getQuantity(),
                result.getAvailableQuantity(), result.getStatus());
    }

}
