package com.jiubuntu.wms.biz.inbound.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InboundLocationResult {

    private final Long locationId;
    private final String locationCode;
    private final Integer quantity;

}
