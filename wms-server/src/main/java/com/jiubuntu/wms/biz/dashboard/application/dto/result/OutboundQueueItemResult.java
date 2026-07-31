package com.jiubuntu.wms.biz.dashboard.application.dto.result;

import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutboundQueueItemResult {

    private final Long id;
    private final String customerName;
    private final long itemCount;
    private final long waitingMinutes;
    private final OutboundStatus status;

}
