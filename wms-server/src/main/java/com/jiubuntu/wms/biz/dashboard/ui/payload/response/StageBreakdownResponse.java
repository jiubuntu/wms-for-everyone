package com.jiubuntu.wms.biz.dashboard.ui.payload.response;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.StageBreakdownResult;
import lombok.Getter;

@Getter
public class StageBreakdownResponse {

    private final long pending;
    private final long inProgress;
    private final long completed;
    private final long cancelled;

    private StageBreakdownResponse(long pending, long inProgress, long completed, long cancelled) {
        this.pending = pending;
        this.inProgress = inProgress;
        this.completed = completed;
        this.cancelled = cancelled;
    }

    public static StageBreakdownResponse from(StageBreakdownResult result) {
        return new StageBreakdownResponse(result.getPending(), result.getInProgress(), result.getCompleted(), result.getCancelled());
    }

}
