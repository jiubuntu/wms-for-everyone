package com.jiubuntu.wms.biz.user.ui;

import com.jiubuntu.wms.biz.user.application.UserService;
import com.jiubuntu.wms.biz.user.application.dto.command.UserChangePasswordCommand;
import com.jiubuntu.wms.biz.user.application.dto.command.UserIssueCommand;
import com.jiubuntu.wms.biz.user.application.dto.result.UserIssueResult;
import com.jiubuntu.wms.biz.user.application.dto.result.UserListResult;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.ui.payload.request.UserChangePasswordRequest;
import com.jiubuntu.wms.biz.user.ui.payload.request.UserIssueRequest;
import com.jiubuntu.wms.biz.user.ui.payload.response.UserIssueResponse;
import com.jiubuntu.wms.biz.user.ui.payload.response.UserListResponse;
import com.jiubuntu.wms.biz.user.ui.payload.response.UserWithdrawResponse;
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

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER})
    @GetMapping("/list")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<UserListResponse>>> list(
            AuthPrincipal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = PageConstants.DEFAULT_LIMIT) int limit
    ) {
        Page<UserListResult> results = userService.list(
                principal.getCompanyId(), principal.getRole(), principal.getWarehouseId(), keyword, PageRequest.of(page - 1, limit));
        Page<UserListResponse> mapped = results.map(UserListResponse::from);

        ApiCommonResponse<ApiPageResponse<UserListResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER})
    @PostMapping("/create")
    public ResponseEntity<ApiCommonResponse<UserIssueResponse>> create(
            AuthPrincipal principal,
            @Valid @RequestBody UserIssueRequest request
    ) {
        UserIssueCommand command = new UserIssueCommand(
                principal.getUserId(),
                request.getEmail(),
                request.getName(),
                request.getPhone(),
                request.getRole(),
                request.getWarehouseId()
        );
        UserIssueResult result = userService.issueAccount(command);

        ApiCommonResponse<UserIssueResponse> body = ApiCommonResponse.success(
                UserIssueResponse.from(result.getUser(), result.getTemporaryPassword()), ResponseCode.CREATED);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<ApiCommonResponse<UserWithdrawResponse>> withdraw(
            AuthPrincipal principal,
            @PathVariable Long id
    ) {
        User user = userService.withdraw(principal.getUserId(), id);

        ApiCommonResponse<UserWithdrawResponse> body = ApiCommonResponse.success(UserWithdrawResponse.from(user));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiCommonResponse<Void>> changePassword(
            AuthPrincipal principal,
            @Valid @RequestBody UserChangePasswordRequest request
    ) {
        UserChangePasswordCommand command = new UserChangePasswordCommand(
                principal.getUserId(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );
        userService.changePassword(command);

        ApiCommonResponse<Void> body = ApiCommonResponse.success(null);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
