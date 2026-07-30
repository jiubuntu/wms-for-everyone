package com.jiubuntu.wms.admin.ui.company;

import com.jiubuntu.wms.admin.application.company.CompanyAdminService;
import com.jiubuntu.wms.admin.ui.company.payload.response.CompanyAdminApproveResponse;
import com.jiubuntu.wms.admin.ui.company.payload.response.CompanyAdminDetailResponse;
import com.jiubuntu.wms.admin.ui.company.payload.response.CompanyAdminListItemResponse;
import com.jiubuntu.wms.biz.company.application.dto.result.CompanyResult;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.global.payload.constants.PageConstants;
import com.jiubuntu.wms.global.payload.response.ApiCommonResponse;
import com.jiubuntu.wms.global.payload.response.ApiPageResponse;
import com.jiubuntu.wms.global.security.authentication.AuthPrincipal;
import com.jiubuntu.wms.global.security.resolver.annotation.Secure;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Secure(UserRole.SYSTEM_ADMIN)
@RestController
@RequestMapping("/api/admin/company")
@RequiredArgsConstructor
public class CompanyAdminController {

    private final CompanyAdminService companyAdminService;

    @GetMapping("/list")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<CompanyAdminListItemResponse>>> list(
            AuthPrincipal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = PageConstants.DEFAULT_LIMIT) int limit
    ) {
        Page<CompanyResult> pendingList = companyAdminService.findPendingList(keyword, PageRequest.of(page - 1, limit));
        Page<CompanyAdminListItemResponse> mapped = pendingList.map(CompanyAdminListItemResponse::from);

        ApiCommonResponse<ApiPageResponse<CompanyAdminListItemResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @GetMapping
    public ResponseEntity<ApiCommonResponse<CompanyAdminDetailResponse>> detail(
            AuthPrincipal principal,
            @RequestParam Long companyId
    ) {
        Company company = companyAdminService.getDetail(companyId);
        String businessLicenseFileUrl = companyAdminService.getBusinessLicenseFileUrl(companyId);

        ApiCommonResponse<CompanyAdminDetailResponse> body = ApiCommonResponse.success(CompanyAdminDetailResponse.of(company, businessLicenseFileUrl));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiCommonResponse<CompanyAdminApproveResponse>> approve(
            AuthPrincipal principal,
            @PathVariable Long id
    ) {
        Company company = companyAdminService.approve(id, principal.getUserId());

        ApiCommonResponse<CompanyAdminApproveResponse> body = ApiCommonResponse.success(CompanyAdminApproveResponse.from(company));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
