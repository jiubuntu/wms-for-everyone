package com.jiubuntu.wms.biz.outbound.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutboundAllocationResult {

    private final String locationCode;
    private final String lotNumber;
    private final Integer quantity;

}
