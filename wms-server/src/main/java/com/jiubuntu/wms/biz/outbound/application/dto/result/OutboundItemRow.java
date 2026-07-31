package com.jiubuntu.wms.biz.outbound.application.dto.result;

import com.jiubuntu.wms.biz.outbound.domain.AllocationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 출고 상세 조회 시 allocations를 붙이기 전 상품 라인 중간 결과.
 */
@Getter
@AllArgsConstructor
public class OutboundItemRow {

    private final Long id;
    private final Long productId;
    private final String productSkuCode;
    private final String productName;
    private final Long unitId;
    private final String unitName;
    private final Integer quantity;
    private final AllocationType allocationType;

}
