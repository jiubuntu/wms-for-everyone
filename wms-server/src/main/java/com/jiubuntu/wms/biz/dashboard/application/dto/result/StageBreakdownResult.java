package com.jiubuntu.wms.biz.dashboard.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StageBreakdownResult {

    private final long pending;
    private final long inProgress;
    private final long completed;
    private final long cancelled;

}
