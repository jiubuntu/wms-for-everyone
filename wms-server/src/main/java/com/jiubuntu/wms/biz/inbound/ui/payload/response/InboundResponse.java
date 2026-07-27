package com.jiubuntu.wms.biz.inbound.ui.payload.response;

import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundResult;
import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class InboundResponse {

    private final Long id;
    private final Long warehouseId;
    private final String supplierName;
    private final InboundStatus status;
    private final String note;
    private final String processedByName;
    private final LocalDateTime createdAt;
    private final List<InboundItemResponse> items;

    private InboundResponse(Long id, Long warehouseId, String supplierName, InboundStatus status, String note,
                             String processedByName, LocalDateTime createdAt, List<InboundItemResponse> items) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.supplierName = supplierName;
        this.status = status;
        this.note = note;
        this.processedByName = processedByName;
        this.createdAt = createdAt;
        this.items = items;
    }

    public static InboundResponse from(InboundResult result) {
        return new InboundResponse(
                result.getId(),
                result.getWarehouseId(),
                result.getSupplierName(),
                result.getStatus(),
                result.getNote(),
                result.getProcessedByName(),
                result.getCreatedAt(),
                result.getItems().stream().map(InboundItemResponse::from).toList()
        );
    }

}
