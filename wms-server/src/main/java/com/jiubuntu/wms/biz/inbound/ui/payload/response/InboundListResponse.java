package com.jiubuntu.wms.biz.inbound.ui.payload.response;

import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundListResult;
import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InboundListResponse {

    private final Long id;
    private final String supplierName;
    private final InboundStatus status;
    private final Long itemCount;
    private final LocalDateTime createdAt;

    private InboundListResponse(Long id, String supplierName, InboundStatus status, Long itemCount, LocalDateTime createdAt) {
        this.id = id;
        this.supplierName = supplierName;
        this.status = status;
        this.itemCount = itemCount;
        this.createdAt = createdAt;
    }

    public static InboundListResponse from(InboundListResult result) {
        return new InboundListResponse(result.getId(), result.getSupplierName(), result.getStatus(),
                result.getItemCount(), result.getCreatedAt());
    }

}
