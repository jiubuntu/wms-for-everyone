package com.jiubuntu.wms.biz.inventory.ui.payload.response;

import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryResult;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class InventoryResponse {

    private final Long id;
    private final Long warehouseId;
    private final Long locationId;
    private final String locationCode;
    private final Long productId;
    private final String productSkuCode;
    private final String productName;
    private final String unitName;
    private final String lotNumber;
    private final LocalDate manufactureDate;
    private final LocalDate expiryDate;
    private final int quantity;
    private final int reservedQuantity;

    private InventoryResponse(Long id, Long warehouseId, Long locationId, String locationCode, Long productId,
                               String productSkuCode, String productName, String unitName, String lotNumber,
                               LocalDate manufactureDate, LocalDate expiryDate, int quantity, int reservedQuantity) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.locationId = locationId;
        this.locationCode = locationCode;
        this.productId = productId;
        this.productSkuCode = productSkuCode;
        this.productName = productName;
        this.unitName = unitName;
        this.lotNumber = lotNumber;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
    }

    public static InventoryResponse from(InventoryResult result) {
        return new InventoryResponse(
                result.getId(),
                result.getWarehouseId(),
                result.getLocationId(),
                result.getLocationCode(),
                result.getProductId(),
                result.getProductSkuCode(),
                result.getProductName(),
                result.getUnitName(),
                result.getLotNumber(),
                result.getManufactureDate(),
                result.getExpiryDate(),
                result.getQuantity(),
                result.getReservedQuantity()
        );
    }

}
