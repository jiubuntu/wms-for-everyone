package com.jiubuntu.wms.biz.auth.presentation.payload;

import com.jiubuntu.wms.biz.user.domain.User;
import lombok.Getter;

@Getter
public class AuthLoginResponse {

    private final String accessToken;
    private final Long userId;
    private final String email;
    private final String name;
    private final String role;

    private AuthLoginResponse(String accessToken, Long userId, String email, String name, String role) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public static AuthLoginResponse of(String accessToken, User user) {
        return new AuthLoginResponse(accessToken, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }

}
