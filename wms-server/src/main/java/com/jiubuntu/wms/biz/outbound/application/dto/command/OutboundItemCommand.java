package com.jiubuntu.wms.biz.outbound.application.dto.command;

import com.jiubuntu.wms.biz.outbound.domain.AllocationType;

import java.util.List;

import lombok.Getter;

@Getter
public class OutboundItemCommand {

    private final Long productId;
    private final Long unitId;
    private final int quantity;
    private final AllocationType allocationType;
    private final List<OutboundAllocationCommand> allocations;

    public OutboundItemCommand(Long productId, Long unitId, int quantity, AllocationType allocationType,
                                List<OutboundAllocationCommand> allocations) {
        this.productId = productId;
        this.unitId = unitId;
        this.quantity = quantity;
        this.allocationType = allocationType;
        this.allocations = allocations;
    }

}
