package com.jiubuntu.wms.biz.inbound.application.dto.command;

import lombok.Getter;

@Getter
public class InboundLocationCommand {

    private final Long locationId;
    private final int quantity;

    public InboundLocationCommand(Long locationId, int quantity) {
        this.locationId = locationId;
        this.quantity = quantity;
    }

}
