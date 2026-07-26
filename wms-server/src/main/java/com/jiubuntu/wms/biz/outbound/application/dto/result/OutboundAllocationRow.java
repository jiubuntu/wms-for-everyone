package com.jiubuntu.wms.biz.outbound.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 상품 라인별로 그룹핑하기 전, outboundItemId를 포함한 할당 내역 중간 결과.
 */
@Getter
@AllArgsConstructor
public class OutboundAllocationRow {

    private final Long outboundItemId;
    private final String locationCode;
    private final String lotNumber;
    private final Integer quantity;

}
