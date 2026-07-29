package com.jiubuntu.wms.biz.dashboard.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExpiringInventoryItemResult {

    private final String productName;
    private final String lotNumber;
    private final String locationCode;
    private final int quantity;
    private final long daysLeft;

}
