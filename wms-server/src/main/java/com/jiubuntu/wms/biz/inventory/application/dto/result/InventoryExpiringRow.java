package com.jiubuntu.wms.biz.inventory.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class InventoryExpiringRow {

    private final String productName;
    private final String lotNumber;
    private final String locationCode;
    private final int quantity;
    private final LocalDate expiryDate;

}
