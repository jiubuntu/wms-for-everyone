package com.jiubuntu.wms.biz.user.ui.payload.response;

import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import lombok.Getter;

@Getter
public class UserWithdrawResponse {

    private final Long id;
    private final UserStatus status;

    private UserWithdrawResponse(Long id, UserStatus status) {
        this.id = id;
        this.status = status;
    }

    public static UserWithdrawResponse from(User user) {
        return new UserWithdrawResponse(user.getId(), user.getStatus());
    }

}
