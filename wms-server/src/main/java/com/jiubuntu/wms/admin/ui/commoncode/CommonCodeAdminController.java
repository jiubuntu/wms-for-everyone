package com.jiubuntu.wms.admin.ui.commoncode;

import com.jiubuntu.wms.admin.application.commoncode.CommonCodeAdminService;
import com.jiubuntu.wms.biz.commoncode.application.dto.result.CommonCodeResult;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.commoncode.ui.payload.request.CommonCodeCreateRequest;
import com.jiubuntu.wms.biz.commoncode.ui.payload.request.CommonCodeDeleteRequest;
import com.jiubuntu.wms.biz.commoncode.ui.payload.request.CommonCodeUpdateRequest;
import com.jiubuntu.wms.biz.commoncode.ui.payload.response.CommonCodeResponse;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.global.payload.constants.ResponseCode;
import com.jiubuntu.wms.global.payload.response.ApiCommonResponse;
import com.jiubuntu.wms.global.payload.response.ApiPageResponse;
import com.jiubuntu.wms.global.security.authentication.AuthPrincipal;
import com.jiubuntu.wms.global.security.resolver.annotation.Secure;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Secure(UserRole.SYSTEM_ADMIN)
@RestController
@RequestMapping("/api/admin/common-code")
@RequiredArgsConstructor
public class CommonCodeAdminController {

    private final CommonCodeAdminService commonCodeAdminService;

    @GetMapping("/list")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<CommonCodeResponse>>> list(
            AuthPrincipal principal,
            @RequestParam CommonCodeGroup groupCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<CommonCodeResult> results = commonCodeAdminService.list(groupCode, PageRequest.of(page - 1, limit));
        Page<CommonCodeResponse> mapped = results.map(CommonCodeResponse::from);

        ApiCommonResponse<ApiPageResponse<CommonCodeResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiCommonResponse<CommonCodeResponse>> create(
            AuthPrincipal principal,
            @Valid @RequestBody CommonCodeCreateRequest request
    ) {
        CommonCode commonCode = commonCodeAdminService.create(
                request.getGroupCode(), request.getCode(), request.getName(), request.getSortOrder());

        ApiCommonResponse<CommonCodeResponse> body = ApiCommonResponse.success(CommonCodeResponse.from(commonCode), ResponseCode.CREATED);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<ApiCommonResponse<CommonCodeResponse>> update(
            AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CommonCodeUpdateRequest request
    ) {
        CommonCode commonCode = commonCodeAdminService.update(id, request.getName(), request.getSortOrder(), principal.getUserId());

        ApiCommonResponse<CommonCodeResponse> body = ApiCommonResponse.success(CommonCodeResponse.from(commonCode));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiCommonResponse<Void>> delete(
            AuthPrincipal principal,
            @Valid @RequestBody CommonCodeDeleteRequest request
    ) {
        commonCodeAdminService.delete(request.getId(), principal.getUserId());

        ApiCommonResponse<Void> body = ApiCommonResponse.success(null);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
