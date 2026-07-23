package com.jiubuntu.wms.biz.commoncode.ui;

import com.jiubuntu.wms.biz.commoncode.application.CommonCodeService;
import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeCreateCommand;
import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeDeleteCommand;
import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeUpdateCommand;
import com.jiubuntu.wms.biz.commoncode.application.dto.result.CommonCodeResult;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.commoncode.ui.payload.request.CommonCodeCreateRequest;
import com.jiubuntu.wms.biz.commoncode.ui.payload.request.CommonCodeDeleteRequest;
import com.jiubuntu.wms.biz.commoncode.ui.payload.request.CommonCodeUpdateRequest;
import com.jiubuntu.wms.biz.commoncode.ui.payload.response.CommonCodeResponse;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.global.payload.constants.PageConstants;
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

import java.util.List;

@Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER})
@RestController
@RequestMapping("/api/common-code")
@RequiredArgsConstructor
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @GetMapping("/list")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<CommonCodeResponse>>> list(
            AuthPrincipal principal,
            @RequestParam CommonCodeGroup groupCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = PageConstants.DEFAULT_LIMIT) int limit
    ) {
        Page<CommonCodeResult> results = commonCodeService.list(groupCode, principal.getCompanyId(), PageRequest.of(page - 1, limit));
        Page<CommonCodeResponse> mapped = results.map(CommonCodeResponse::from);

        ApiCommonResponse<ApiPageResponse<CommonCodeResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiCommonResponse<List<CommonCodeResponse>>> listAll(
            AuthPrincipal principal,
            @RequestParam CommonCodeGroup groupCode
    ) {
        List<CommonCodeResponse> results = commonCodeService.listAll(groupCode, principal.getCompanyId()).stream()
                .map(CommonCodeResponse::from)
                .toList();

        ApiCommonResponse<List<CommonCodeResponse>> body = ApiCommonResponse.success(results);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiCommonResponse<CommonCodeResponse>> create(
            AuthPrincipal principal,
            @Valid @RequestBody CommonCodeCreateRequest request
    ) {
        CommonCodeCreateCommand command = new CommonCodeCreateCommand(
                principal.getCompanyId(),
                request.getGroupCode(),
                request.getCode(),
                request.getName(),
                request.getSortOrder()
        );
        CommonCode commonCode = commonCodeService.create(command);

        ApiCommonResponse<CommonCodeResponse> body = ApiCommonResponse.success(CommonCodeResponse.from(commonCode), ResponseCode.CREATED);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<ApiCommonResponse<CommonCodeResponse>> update(
            AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CommonCodeUpdateRequest request
    ) {
        CommonCodeUpdateCommand command = new CommonCodeUpdateCommand(
                id,
                principal.getCompanyId(),
                request.getName(),
                request.getSortOrder(),
                principal.getUserId()
        );
        CommonCode commonCode = commonCodeService.update(command);

        ApiCommonResponse<CommonCodeResponse> body = ApiCommonResponse.success(CommonCodeResponse.from(commonCode));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiCommonResponse<Void>> delete(
            AuthPrincipal principal,
            @Valid @RequestBody CommonCodeDeleteRequest request
    ) {
        CommonCodeDeleteCommand command = new CommonCodeDeleteCommand(request.getId(), principal.getCompanyId(), principal.getUserId());
        commonCodeService.delete(command);

        ApiCommonResponse<Void> body = ApiCommonResponse.success(null);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
