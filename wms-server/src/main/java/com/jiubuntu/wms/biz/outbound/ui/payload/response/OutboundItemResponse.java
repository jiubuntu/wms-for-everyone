package com.jiubuntu.wms.biz.outbound.ui.payload.response;

import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundItemResult;
import com.jiubuntu.wms.biz.outbound.domain.AllocationType;
import lombok.Getter;

import java.util.List;

@Getter
public class OutboundItemResponse {

    private final Long id;
    private final Long productId;
    private final String productSkuCode;
    private final String productName;
    private final Long unitId;
    private final String unitName;
    private final Integer quantity;
    private final AllocationType allocationType;
    private final List<OutboundAllocationResponse> allocations;

    private OutboundItemResponse(Long id, Long productId, String productSkuCode, String productName, Long unitId,
                                  String unitName, Integer quantity, AllocationType allocationType,
                                  List<OutboundAllocationResponse> allocations) {
        this.id = id;
        this.productId = productId;
        this.productSkuCode = productSkuCode;
        this.productName = productName;
        this.unitId = unitId;
        this.unitName = unitName;
        this.quantity = quantity;
        this.allocationType = allocationType;
        this.allocations = allocations;
    }

    public static OutboundItemResponse from(OutboundItemResult result) {
        return new OutboundItemResponse(
                result.getId(),
                result.getProductId(),
                result.getProductSkuCode(),
                result.getProductName(),
                result.getUnitId(),
                result.getUnitName(),
                result.getQuantity(),
                result.getAllocationType(),
                result.getAllocations().stream().map(OutboundAllocationResponse::from).toList()
        );
    }

}
