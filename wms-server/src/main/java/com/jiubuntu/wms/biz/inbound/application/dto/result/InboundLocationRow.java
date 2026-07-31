package com.jiubuntu.wms.biz.inbound.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 상품 라인별로 그룹핑하기 전, inboundItemId를 포함한 위치 배치 중간 결과.
 */
@Getter
@AllArgsConstructor
public class InboundLocationRow {

    private final Long inboundItemId;
    private final Long locationId;
    private final String locationCode;
    private final Integer quantity;

}
