package com.jiubuntu.wms.biz.dashboard.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WarehouseScopeStatsResult {

    private final long outboundPending;
    private final long outboundPicking;
    private final long outboundCompletedToday;
    private final long inboundPending;
    private final long expiringSoonCount;

}
