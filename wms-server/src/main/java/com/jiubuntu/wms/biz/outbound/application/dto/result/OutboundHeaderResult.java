package com.jiubuntu.wms.biz.outbound.application.dto.result;

import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 출고 상세 조회 시 items 리스트를 별도 쿼리로 조립하기 전, 헤더 필드만 먼저 읽어오는 중간 결과.
 */
@Getter
@AllArgsConstructor
public class OutboundHeaderResult {

    private final Long id;
    private final Long warehouseId;
    private final String customerName;
    private final OutboundStatus status;
    private final String note;
    private final String processedByName;
    private final LocalDateTime createdAt;

}
