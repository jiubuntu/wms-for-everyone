package com.jiubuntu.wms.biz.dashboard.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CompanyOverviewSummaryResult {

    private final long productSkuCount;
    private final CompanyStatsResult companyStats;
    private final List<WarehouseOpsRowResult> warehouseOps;
    private final List<ProcessingTrendPointResult> processingTrend;
    private final StageBreakdownResult todayOutboundStatus;
    private final StageBreakdownResult todayInboundStatus;

}
