package com.jiubuntu.wms.biz.outbound.ui.payload.response;

import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundResult;
import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OutboundResponse {

    private final Long id;
    private final Long warehouseId;
    private final String customerName;
    private final OutboundStatus status;
    private final String note;
    private final String processedByName;
    private final LocalDateTime createdAt;
    private final List<OutboundItemResponse> items;

    private OutboundResponse(Long id, Long warehouseId, String customerName, OutboundStatus status, String note,
                              String processedByName, LocalDateTime createdAt, List<OutboundItemResponse> items) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.customerName = customerName;
        this.status = status;
        this.note = note;
        this.processedByName = processedByName;
        this.createdAt = createdAt;
        this.items = items;
    }

    public static OutboundResponse from(OutboundResult result) {
        return new OutboundResponse(
                result.getId(),
                result.getWarehouseId(),
                result.getCustomerName(),
                result.getStatus(),
                result.getNote(),
                result.getProcessedByName(),
                result.getCreatedAt(),
                result.getItems().stream().map(OutboundItemResponse::from).toList()
        );
    }

}
