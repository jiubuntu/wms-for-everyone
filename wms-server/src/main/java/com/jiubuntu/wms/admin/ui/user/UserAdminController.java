package com.jiubuntu.wms.admin.ui.user;

import com.jiubuntu.wms.admin.application.user.UserAdminService;
import com.jiubuntu.wms.admin.ui.user.payload.response.UserAdminWithdrawResponse;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.global.payload.response.ApiCommonResponse;
import com.jiubuntu.wms.global.security.authentication.AuthPrincipal;
import com.jiubuntu.wms.global.security.resolver.annotation.Secure;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Secure(UserRole.SYSTEM_ADMIN)
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<ApiCommonResponse<UserAdminWithdrawResponse>> withdraw(
            AuthPrincipal principal,
            @PathVariable Long id
    ) {
        User user = userAdminService.withdraw(id, principal.getUserId());

        ApiCommonResponse<UserAdminWithdrawResponse> body = ApiCommonResponse.success(UserAdminWithdrawResponse.from(user));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
