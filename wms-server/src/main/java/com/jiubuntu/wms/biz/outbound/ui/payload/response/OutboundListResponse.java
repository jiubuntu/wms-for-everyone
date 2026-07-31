package com.jiubuntu.wms.biz.outbound.ui.payload.response;

import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundListResult;
import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OutboundListResponse {

    private final Long id;
    private final String customerName;
    private final OutboundStatus status;
    private final Long itemCount;
    private final LocalDateTime createdAt;

    private OutboundListResponse(Long id, String customerName, OutboundStatus status, Long itemCount, LocalDateTime createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.status = status;
        this.itemCount = itemCount;
        this.createdAt = createdAt;
    }

    public static OutboundListResponse from(OutboundListResult result) {
        return new OutboundListResponse(result.getId(), result.getCustomerName(), result.getStatus(),
                result.getItemCount(), result.getCreatedAt());
    }

}
