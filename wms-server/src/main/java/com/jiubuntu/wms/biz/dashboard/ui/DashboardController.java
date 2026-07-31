package com.jiubuntu.wms.biz.dashboard.ui;

import com.jiubuntu.wms.biz.dashboard.application.DashboardService;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.CompanyOverviewSummaryResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.WarehouseScopeSummaryResult;
import com.jiubuntu.wms.biz.dashboard.ui.payload.response.CompanyOverviewSummaryResponse;
import com.jiubuntu.wms.biz.dashboard.ui.payload.response.WarehouseScopeSummaryResponse;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.global.payload.response.ApiCommonResponse;
import com.jiubuntu.wms.global.security.authentication.AuthPrincipal;
import com.jiubuntu.wms.global.security.resolver.annotation.Secure;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER, UserRole.WORKER})
    @GetMapping("/api/warehouse/{warehouseId}/dashboard/summary")
    public ResponseEntity<ApiCommonResponse<WarehouseScopeSummaryResponse>> getWarehouseScopeSummary(
            AuthPrincipal principal,
            @PathVariable Long warehouseId
    ) {
        WarehouseScopeSummaryResult result = dashboardService.getWarehouseScopeSummary(
                warehouseId, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId());

        ApiCommonResponse<WarehouseScopeSummaryResponse> body =
                ApiCommonResponse.success(WarehouseScopeSummaryResponse.from(result));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Secure(UserRole.COMPANY_ADMIN)
    @GetMapping("/api/company/dashboard/summary")
    public ResponseEntity<ApiCommonResponse<CompanyOverviewSummaryResponse>> getCompanyOverviewSummary(
            AuthPrincipal principal
    ) {
        CompanyOverviewSummaryResult result = dashboardService.getCompanyOverviewSummary(
                principal.getCompanyId(), principal.getRole(), principal.getWarehouseId());

        ApiCommonResponse<CompanyOverviewSummaryResponse> body =
                ApiCommonResponse.success(CompanyOverviewSummaryResponse.from(result));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
