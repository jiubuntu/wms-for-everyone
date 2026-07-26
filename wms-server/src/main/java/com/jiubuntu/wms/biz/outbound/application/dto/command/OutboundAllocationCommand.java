package com.jiubuntu.wms.biz.outbound.application.dto.command;

import lombok.Getter;

@Getter
public class OutboundAllocationCommand {

    private final Long locationId;
    private final String lotNumber;
    private final int quantity;

    public OutboundAllocationCommand(Long locationId, String lotNumber, int quantity) {
        this.locationId = locationId;
        this.lotNumber = lotNumber;
        this.quantity = quantity;
    }

}
