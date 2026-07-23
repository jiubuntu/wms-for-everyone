package com.jiubuntu.wms.biz.inventory.application.dto.result;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AvailableLocationResult {

    private final Long locationId;
    private final String locationCode;
    private final String lotNumber;
    private final LocalDate expiryDate;
    private final Integer availableQuantity;

    public AvailableLocationResult(Long locationId, String locationCode, String lotNumber, LocalDate expiryDate,
                                    Integer availableQuantity) {
        this.locationId = locationId;
        this.locationCode = locationCode;
        this.lotNumber = lotNumber;
        this.expiryDate = expiryDate;
        this.availableQuantity = availableQuantity;
    }

}
