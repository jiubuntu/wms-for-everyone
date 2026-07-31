package com.jiubuntu.wms.biz.inventory.application.dto.result;

import com.jiubuntu.wms.biz.inventory.domain.InventoryHistoryTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class InventoryHistoryResult {

    private final Long id;
    private final Long warehouseId;
    private final String locationCode;
    private final String productSkuCode;
    private final String productName;
    private final String lotNumber;
    private final Integer quantityChange;
    private final Integer quantityAfter;
    private final InventoryHistoryTargetType targetType;
    private final Long targetId;
    private final String reason;
    private final LocalDateTime createdAt;
    private final String createdByName;

}
