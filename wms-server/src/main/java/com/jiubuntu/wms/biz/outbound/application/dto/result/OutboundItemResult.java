package com.jiubuntu.wms.biz.outbound.application.dto.result;

import com.jiubuntu.wms.biz.outbound.domain.AllocationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OutboundItemResult {

    private final Long id;
    private final Long productId;
    private final String productSkuCode;
    private final String productName;
    private final Long unitId;
    private final String unitName;
    private final Integer quantity;
    private final AllocationType allocationType;
    private final List<OutboundAllocationResult> allocations;

}
