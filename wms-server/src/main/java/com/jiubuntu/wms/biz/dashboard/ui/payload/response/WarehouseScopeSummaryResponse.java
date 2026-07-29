package com.jiubuntu.wms.biz.dashboard.ui.payload.response;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.WarehouseScopeSummaryResult;
import lombok.Getter;

import java.util.List;

@Getter
public class WarehouseScopeSummaryResponse {

    private final WarehouseScopeStatsResponse stats;
    private final List<OutboundQueueItemResponse> outboundQueue;
    private final List<InboundQueueItemResponse> inboundQueue;
    private final List<ExpiringInventoryItemResponse> expiringInventory;
    private final List<InventoryStatusItemResponse> inventoryStatus;

    private WarehouseScopeSummaryResponse(WarehouseScopeStatsResponse stats, List<OutboundQueueItemResponse> outboundQueue,
                                           List<InboundQueueItemResponse> inboundQueue,
                                           List<ExpiringInventoryItemResponse> expiringInventory,
                                           List<InventoryStatusItemResponse> inventoryStatus) {
        this.stats = stats;
        this.outboundQueue = outboundQueue;
        this.inboundQueue = inboundQueue;
        this.expiringInventory = expiringInventory;
        this.inventoryStatus = inventoryStatus;
    }

    public static WarehouseScopeSummaryResponse from(WarehouseScopeSummaryResult result) {
        return new WarehouseScopeSummaryResponse(
                WarehouseScopeStatsResponse.from(result.getStats()),
                result.getOutboundQueue().stream().map(OutboundQueueItemResponse::from).toList(),
                result.getInboundQueue().stream().map(InboundQueueItemResponse::from).toList(),
                result.getExpiringInventory().stream().map(ExpiringInventoryItemResponse::from).toList(),
                result.getInventoryStatus().stream().map(InventoryStatusItemResponse::from).toList()
        );
    }

}
