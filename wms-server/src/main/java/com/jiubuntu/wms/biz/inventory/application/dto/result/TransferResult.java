package com.jiubuntu.wms.biz.inventory.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TransferResult {

    private final Long id;
    private final Long warehouseId;
    private final Long productId;
    private final String productSkuCode;
    private final String productName;
    private final String unitName;
    private final Long fromLocationId;
    private final String fromLocationCode;
    private final Long toLocationId;
    private final String toLocationCode;
    private final String lotNumber;
    private final Integer quantity;
    private final Long reasonId;
    private final String note;
    private final String createdByName;
    private final LocalDateTime createdAt;

}
