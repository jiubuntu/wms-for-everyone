package com.jiubuntu.wms.biz.auth.application.dto;

import com.jiubuntu.wms.biz.user.domain.User;
import lombok.Getter;

@Getter
public class LoginResult {

    private final String accessToken;
    private final String refreshToken;
    private final long refreshTokenExpirationMillis;
    private final User user;

    public LoginResult(String accessToken, String refreshToken, long refreshTokenExpirationMillis, User user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
        this.user = user;
    }

}
