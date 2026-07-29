package com.jiubuntu.wms.biz.dashboard.ui.payload.response;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.WarehouseScopeStatsResult;
import lombok.Getter;

@Getter
public class WarehouseScopeStatsResponse {

    private final long outboundPending;
    private final long outboundPicking;
    private final long outboundCompletedToday;
    private final long inboundPending;
    private final long expiringSoonCount;

    private WarehouseScopeStatsResponse(long outboundPending, long outboundPicking, long outboundCompletedToday,
                                         long inboundPending, long expiringSoonCount) {
        this.outboundPending = outboundPending;
        this.outboundPicking = outboundPicking;
        this.outboundCompletedToday = outboundCompletedToday;
        this.inboundPending = inboundPending;
        this.expiringSoonCount = expiringSoonCount;
    }

    public static WarehouseScopeStatsResponse from(WarehouseScopeStatsResult result) {
        return new WarehouseScopeStatsResponse(
                result.getOutboundPending(),
                result.getOutboundPicking(),
                result.getOutboundCompletedToday(),
                result.getInboundPending(),
                result.getExpiringSoonCount()
        );
    }

}
