package com.jiubuntu.wms.biz.inbound.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 입고 상세 조회 시 locations를 붙이기 전 상품 라인 중간 결과.
 */
@Getter
@AllArgsConstructor
public class InboundItemRow {

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

}
