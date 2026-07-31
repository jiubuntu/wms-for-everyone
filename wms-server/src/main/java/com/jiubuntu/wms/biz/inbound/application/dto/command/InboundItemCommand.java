package com.jiubuntu.wms.biz.inbound.application.dto.command;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class InboundItemCommand {

    private final Long productId;
    private final Long unitId;
    private final int quantity;
    private final String lotNumber;
    private final LocalDate manufactureDate;
    private final LocalDate expiryDate;

    public InboundItemCommand(Long productId, Long unitId, int quantity, String lotNumber,
                               LocalDate manufactureDate, LocalDate expiryDate) {
        this.productId = productId;
        this.unitId = unitId;
        this.quantity = quantity;
        this.lotNumber = lotNumber;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
    }

}
