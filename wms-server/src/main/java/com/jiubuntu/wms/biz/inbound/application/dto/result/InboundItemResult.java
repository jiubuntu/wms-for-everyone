package com.jiubuntu.wms.biz.inbound.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class InboundItemResult {

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
    private final List<InboundLocationResult> locations;

}
