package com.jiubuntu.wms.biz.inventory.ui.payload.response;

import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryHistoryResult;
import com.jiubuntu.wms.biz.inventory.domain.InventoryHistoryTargetType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InventoryHistoryResponse {

    private final Long id;
    private final Long warehouseId;
    private final String locationCode;
    private final String productSkuCode;
    private final String productName;
    private final String lotNumber;
    private final int quantityChange;
    private final int quantityAfter;
    private final InventoryHistoryTargetType targetType;
    private final Long targetId;
    private final String reason;
    private final LocalDateTime createdAt;
    private final String createdByName;

    private InventoryHistoryResponse(Long id, Long warehouseId, String locationCode, String productSkuCode,
                                      String productName, String lotNumber, int quantityChange, int quantityAfter,
                                      InventoryHistoryTargetType targetType, Long targetId, String reason,
                                      LocalDateTime createdAt, String createdByName) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.locationCode = locationCode;
        this.productSkuCode = productSkuCode;
        this.productName = productName;
        this.lotNumber = lotNumber;
        this.quantityChange = quantityChange;
        this.quantityAfter = quantityAfter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.createdAt = createdAt;
        this.createdByName = createdByName;
    }

    public static InventoryHistoryResponse from(InventoryHistoryResult result) {
        return new InventoryHistoryResponse(
                result.getId(),
                result.getWarehouseId(),
                result.getLocationCode(),
                result.getProductSkuCode(),
                result.getProductName(),
                result.getLotNumber(),
                result.getQuantityChange(),
                result.getQuantityAfter(),
                result.getTargetType(),
                result.getTargetId(),
                result.getReason(),
                result.getCreatedAt(),
                result.getCreatedByName()
        );
    }

}
