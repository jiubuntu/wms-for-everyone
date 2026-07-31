package com.jiubuntu.wms.biz.inventory.ui.payload.response;

import com.jiubuntu.wms.biz.inventory.application.dto.result.TransferResult;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TransferResponse {

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
    private final int quantity;
    private final Long reasonId;
    private final String note;
    private final String createdByName;
    private final LocalDateTime createdAt;

    private TransferResponse(Long id, Long warehouseId, Long productId, String productSkuCode, String productName,
                              String unitName, Long fromLocationId, String fromLocationCode, Long toLocationId,
                              String toLocationCode, String lotNumber, int quantity, Long reasonId, String note,
                              String createdByName, LocalDateTime createdAt) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.productSkuCode = productSkuCode;
        this.productName = productName;
        this.unitName = unitName;
        this.fromLocationId = fromLocationId;
        this.fromLocationCode = fromLocationCode;
        this.toLocationId = toLocationId;
        this.toLocationCode = toLocationCode;
        this.lotNumber = lotNumber;
        this.quantity = quantity;
        this.reasonId = reasonId;
        this.note = note;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
    }

    public static TransferResponse from(TransferResult result) {
        return new TransferResponse(
                result.getId(),
                result.getWarehouseId(),
                result.getProductId(),
                result.getProductSkuCode(),
                result.getProductName(),
                result.getUnitName(),
                result.getFromLocationId(),
                result.getFromLocationCode(),
                result.getToLocationId(),
                result.getToLocationCode(),
                result.getLotNumber(),
                result.getQuantity(),
                result.getReasonId(),
                result.getNote(),
                result.getCreatedByName(),
                result.getCreatedAt()
        );
    }

}
