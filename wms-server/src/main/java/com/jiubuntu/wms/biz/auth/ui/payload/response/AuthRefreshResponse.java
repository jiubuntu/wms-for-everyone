package com.jiubuntu.wms.biz.auth.ui.payload.response;

import lombok.Getter;

@Getter
public class AuthRefreshResponse {

    private final String accessToken;

    private AuthRefreshResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public static AuthRefreshResponse of(String accessToken) {
        return new AuthRefreshResponse(accessToken);
    }

}
