package com.jiubuntu.wms.admin.ui.user.payload.response;

import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import lombok.Getter;

@Getter
public class UserAdminWithdrawResponse {

    private final Long id;
    private final UserStatus status;

    private UserAdminWithdrawResponse(Long id, UserStatus status) {
        this.id = id;
        this.status = status;
    }

    public static UserAdminWithdrawResponse from(User user) {
        return new UserAdminWithdrawResponse(user.getId(), user.getStatus());
    }

}
