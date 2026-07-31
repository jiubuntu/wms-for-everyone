package com.jiubuntu.wms.biz.outbound.ui.payload.response;

import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundAllocationResult;
import lombok.Getter;

@Getter
public class OutboundAllocationResponse {

    private final String locationCode;
    private final String lotNumber;
    private final Integer quantity;

    private OutboundAllocationResponse(String locationCode, String lotNumber, Integer quantity) {
        this.locationCode = locationCode;
        this.lotNumber = lotNumber;
        this.quantity = quantity;
    }

    public static OutboundAllocationResponse from(OutboundAllocationResult result) {
        return new OutboundAllocationResponse(result.getLocationCode(), result.getLotNumber(), result.getQuantity());
    }

}
