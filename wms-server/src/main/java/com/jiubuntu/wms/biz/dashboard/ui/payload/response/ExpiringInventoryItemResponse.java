package com.jiubuntu.wms.biz.dashboard.ui.payload.response;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.ExpiringInventoryItemResult;
import lombok.Getter;

@Getter
public class ExpiringInventoryItemResponse {

    private final String productName;
    private final String lotNumber;
    private final String locationCode;
    private final int quantity;
    private final long daysLeft;

    private ExpiringInventoryItemResponse(String productName, String lotNumber, String locationCode, int quantity, long daysLeft) {
        this.productName = productName;
        this.lotNumber = lotNumber;
        this.locationCode = locationCode;
        this.quantity = quantity;
        this.daysLeft = daysLeft;
    }

    public static ExpiringInventoryItemResponse from(ExpiringInventoryItemResult result) {
        return new ExpiringInventoryItemResponse(
                result.getProductName(), result.getLotNumber(), result.getLocationCode(),
                result.getQuantity(), result.getDaysLeft());
    }

}
