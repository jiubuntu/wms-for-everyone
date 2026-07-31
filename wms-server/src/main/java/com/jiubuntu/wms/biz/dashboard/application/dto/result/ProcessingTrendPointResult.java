package com.jiubuntu.wms.biz.dashboard.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProcessingTrendPointResult {

    private final String label;
    private final long outboundCount;
    private final long inboundCount;

}
