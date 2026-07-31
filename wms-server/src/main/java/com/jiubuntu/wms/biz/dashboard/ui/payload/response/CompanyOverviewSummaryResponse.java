package com.jiubuntu.wms.biz.dashboard.ui.payload.response;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.CompanyOverviewSummaryResult;
import lombok.Getter;

import java.util.List;

@Getter
public class CompanyOverviewSummaryResponse {

    private final long productSkuCount;
    private final CompanyStatsResponse companyStats;
    private final List<WarehouseOpsRowResponse> warehouseOps;
    private final List<ProcessingTrendPointResponse> processingTrend;
    private final StageBreakdownResponse todayOutboundStatus;
    private final StageBreakdownResponse todayInboundStatus;

    private CompanyOverviewSummaryResponse(long productSkuCount, CompanyStatsResponse companyStats,
                                            List<WarehouseOpsRowResponse> warehouseOps,
                                            List<ProcessingTrendPointResponse> processingTrend,
                                            StageBreakdownResponse todayOutboundStatus,
                                            StageBreakdownResponse todayInboundStatus) {
        this.productSkuCount = productSkuCount;
        this.companyStats = companyStats;
        this.warehouseOps = warehouseOps;
        this.processingTrend = processingTrend;
        this.todayOutboundStatus = todayOutboundStatus;
        this.todayInboundStatus = todayInboundStatus;
    }

    public static CompanyOverviewSummaryResponse from(CompanyOverviewSummaryResult result) {
        return new CompanyOverviewSummaryResponse(
                result.getProductSkuCount(),
                CompanyStatsResponse.from(result.getCompanyStats()),
                result.getWarehouseOps().stream().map(WarehouseOpsRowResponse::from).toList(),
                result.getProcessingTrend().stream().map(ProcessingTrendPointResponse::from).toList(),
                StageBreakdownResponse.from(result.getTodayOutboundStatus()),
                StageBreakdownResponse.from(result.getTodayInboundStatus())
        );
    }

}
