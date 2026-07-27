package com.jiubuntu.wms.biz.inbound.application.dto.result;

import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class InboundResult {

    private final Long id;
    private final Long warehouseId;
    private final String supplierName;
    private final InboundStatus status;
    private final String note;
    private final String processedByName;
    private final LocalDateTime createdAt;
    private final List<InboundItemResult> items;

}
