package com.jiubuntu.wms.biz.auth.presentation.payload;

import com.jiubuntu.wms.biz.user.domain.User;
import lombok.Getter;

@Getter
public class AuthSignupResponse {

    private final Long userId;
    private final String email;
    private final String status;

    private AuthSignupResponse(Long userId, String email, String status) {
        this.userId = userId;
        this.email = email;
        this.status = status;
    }

    public static AuthSignupResponse from(User user) {
        return new AuthSignupResponse(user.getId(), user.getEmail(), user.getStatus().name());
    }

}
