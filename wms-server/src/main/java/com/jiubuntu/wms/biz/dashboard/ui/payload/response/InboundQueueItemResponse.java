package com.jiubuntu.wms.biz.dashboard.ui.payload.response;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.InboundQueueItemResult;
import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InboundQueueItemResponse {

    private final Long id;
    private final String supplierName;
    private final long itemCount;
    private final LocalDateTime createdAt;
    private final InboundStatus status;

    private InboundQueueItemResponse(Long id, String supplierName, long itemCount, LocalDateTime createdAt, InboundStatus status) {
        this.id = id;
        this.supplierName = supplierName;
        this.itemCount = itemCount;
        this.createdAt = createdAt;
        this.status = status;
    }

    public static InboundQueueItemResponse from(InboundQueueItemResult result) {
        return new InboundQueueItemResponse(
                result.getId(), result.getSupplierName(), result.getItemCount(), result.getCreatedAt(), result.getStatus());
    }

}
