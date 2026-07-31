package com.jiubuntu.wms.biz.dashboard.ui.payload.response;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.WarehouseOpsRowResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.WarehouseOpsStatus;
import lombok.Getter;

@Getter
public class WarehouseOpsRowResponse {

    private final Long warehouseId;
    private final String warehouseName;
    private final String warehouseNote;
    private final long outboundPending;
    private final long outboundPicking;
    private final long inboundPending;
    private final long expiringSoonCount;
    private final WarehouseOpsStatus status;

    private WarehouseOpsRowResponse(Long warehouseId, String warehouseName, String warehouseNote, long outboundPending,
                                     long outboundPicking, long inboundPending, long expiringSoonCount, WarehouseOpsStatus status) {
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.warehouseNote = warehouseNote;
        this.outboundPending = outboundPending;
        this.outboundPicking = outboundPicking;
        this.inboundPending = inboundPending;
        this.expiringSoonCount = expiringSoonCount;
        this.status = status;
    }

    public static WarehouseOpsRowResponse from(WarehouseOpsRowResult result) {
        return new WarehouseOpsRowResponse(
                result.getWarehouseId(), result.getWarehouseName(), result.getWarehouseNote(),
                result.getOutboundPending(), result.getOutboundPicking(), result.getInboundPending(),
                result.getExpiringSoonCount(), result.getStatus());
    }

}
