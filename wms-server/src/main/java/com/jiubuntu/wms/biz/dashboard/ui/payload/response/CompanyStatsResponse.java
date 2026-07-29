package com.jiubuntu.wms.biz.dashboard.ui.payload.response;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.CompanyStatsResult;
import lombok.Getter;

@Getter
public class CompanyStatsResponse {

    private final long outboundPending;
    private final long outboundPicking;
    private final long inboundPending;
    private final long expiringSoonCount;

    private CompanyStatsResponse(long outboundPending, long outboundPicking, long inboundPending, long expiringSoonCount) {
        this.outboundPending = outboundPending;
        this.outboundPicking = outboundPicking;
        this.inboundPending = inboundPending;
        this.expiringSoonCount = expiringSoonCount;
    }

    public static CompanyStatsResponse from(CompanyStatsResult result) {
        return new CompanyStatsResponse(
                result.getOutboundPending(), result.getOutboundPicking(), result.getInboundPending(), result.getExpiringSoonCount());
    }

}
