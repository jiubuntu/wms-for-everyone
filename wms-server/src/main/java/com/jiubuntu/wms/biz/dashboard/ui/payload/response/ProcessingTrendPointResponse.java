package com.jiubuntu.wms.biz.dashboard.ui.payload.response;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.ProcessingTrendPointResult;
import lombok.Getter;

@Getter
public class ProcessingTrendPointResponse {

    private final String label;
    private final long outboundCount;
    private final long inboundCount;

    private ProcessingTrendPointResponse(String label, long outboundCount, long inboundCount) {
        this.label = label;
        this.outboundCount = outboundCount;
        this.inboundCount = inboundCount;
    }

    public static ProcessingTrendPointResponse from(ProcessingTrendPointResult result) {
        return new ProcessingTrendPointResponse(result.getLabel(), result.getOutboundCount(), result.getInboundCount());
    }

}
