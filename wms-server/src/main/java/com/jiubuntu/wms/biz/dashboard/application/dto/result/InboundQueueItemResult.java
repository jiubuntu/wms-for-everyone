package com.jiubuntu.wms.biz.dashboard.application.dto.result;

import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InboundQueueItemResult {

    private final Long id;
    private final String supplierName;
    private final long itemCount;
    private final LocalDateTime createdAt;
    private final InboundStatus status;

}
