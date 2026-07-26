package com.jiubuntu.wms.biz.outbound.application.dto.result;

import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OutboundResult {

    private final Long id;
    private final Long warehouseId;
    private final String customerName;
    private final OutboundStatus status;
    private final String note;
    private final String processedByName;
    private final LocalDateTime createdAt;
    private final List<OutboundItemResult> items;

}
