package com.jiubuntu.wms.biz.inbound.ui.payload.response;

import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundLocationResult;
import lombok.Getter;

@Getter
public class InboundLocationResponse {

    private final Long locationId;
    private final String locationCode;
    private final Integer quantity;

    private InboundLocationResponse(Long locationId, String locationCode, Integer quantity) {
        this.locationId = locationId;
        this.locationCode = locationCode;
        this.quantity = quantity;
    }

    public static InboundLocationResponse from(InboundLocationResult result) {
        return new InboundLocationResponse(result.getLocationId(), result.getLocationCode(), result.getQuantity());
    }

}
