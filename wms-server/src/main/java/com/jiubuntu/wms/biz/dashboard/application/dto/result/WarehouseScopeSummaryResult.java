package com.jiubuntu.wms.biz.dashboard.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WarehouseScopeSummaryResult {

    private final WarehouseScopeStatsResult stats;
    private final List<OutboundQueueItemResult> outboundQueue;
    private final List<InboundQueueItemResult> inboundQueue;
    private final List<ExpiringInventoryItemResult> expiringInventory;
    private final List<InventoryStatusItemResult> inventoryStatus;

}
