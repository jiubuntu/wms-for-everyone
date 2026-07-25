package com.jiubuntu.wms.biz.inventory.ui.payload.response;

import com.jiubuntu.wms.biz.inventory.application.dto.result.AvailableLocationResult;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AvailableLocationResponse {

    private final Long locationId;
    private final String locationCode;
    private final String lotNumber;
    private final LocalDate expiryDate;
    private final int availableQuantity;

    private AvailableLocationResponse(Long locationId, String locationCode, String lotNumber, LocalDate expiryDate,
                                       int availableQuantity) {
        this.locationId = locationId;
        this.locationCode = locationCode;
        this.lotNumber = lotNumber;
        this.expiryDate = expiryDate;
        this.availableQuantity = availableQuantity;
    }

    public static AvailableLocationResponse from(AvailableLocationResult result) {
        return new AvailableLocationResponse(
                result.getLocationId(),
                result.getLocationCode(),
                result.getLotNumber(),
                result.getExpiryDate(),
                result.getAvailableQuantity()
        );
    }

}
