package com.jiubuntu.wms.biz.dashboard.ui.payload.response;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.OutboundQueueItemResult;
import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import lombok.Getter;

@Getter
public class OutboundQueueItemResponse {

    private final Long id;
    private final String customerName;
    private final long itemCount;
    private final long waitingMinutes;
    private final OutboundStatus status;

    private OutboundQueueItemResponse(Long id, String customerName, long itemCount, long waitingMinutes, OutboundStatus status) {
        this.id = id;
        this.customerName = customerName;
        this.itemCount = itemCount;
        this.waitingMinutes = waitingMinutes;
        this.status = status;
    }

    public static OutboundQueueItemResponse from(OutboundQueueItemResult result) {
        return new OutboundQueueItemResponse(
                result.getId(), result.getCustomerName(), result.getItemCount(), result.getWaitingMinutes(), result.getStatus());
    }

}
