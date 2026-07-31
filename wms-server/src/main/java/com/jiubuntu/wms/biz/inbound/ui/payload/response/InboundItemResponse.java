package com.jiubuntu.wms.biz.inbound.ui.payload.response;

import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundItemResult;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class InboundItemResponse {

    private final Long id;
    private final Long productId;
    private final String productSkuCode;
    private final String productName;
    private final Long unitId;
    private final String unitName;
    private final Integer quantity;
    private final String lotNumber;
    private final LocalDate manufactureDate;
    private final LocalDate expiryDate;
    private final List<InboundLocationResponse> locations;

    private InboundItemResponse(Long id, Long productId, String productSkuCode, String productName, Long unitId,
                                 String unitName, Integer quantity, String lotNumber, LocalDate manufactureDate,
                                 LocalDate expiryDate, List<InboundLocationResponse> locations) {
        this.id = id;
        this.productId = productId;
        this.productSkuCode = productSkuCode;
        this.productName = productName;
        this.unitId = unitId;
        this.unitName = unitName;
        this.quantity = quantity;
        this.lotNumber = lotNumber;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.locations = locations;
    }

    public static InboundItemResponse from(InboundItemResult result) {
        return new InboundItemResponse(
                result.getId(),
                result.getProductId(),
                result.getProductSkuCode(),
                result.getProductName(),
                result.getUnitId(),
                result.getUnitName(),
                result.getQuantity(),
                result.getLotNumber(),
                result.getManufactureDate(),
                result.getExpiryDate(),
                result.getLocations().stream().map(InboundLocationResponse::from).toList()
        );
    }

}
