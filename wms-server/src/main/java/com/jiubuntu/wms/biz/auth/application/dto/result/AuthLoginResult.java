package com.jiubuntu.wms.biz.auth.application.dto.result;

import com.jiubuntu.wms.biz.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthLoginResult {

    private final String accessToken;
    private final String refreshToken;
    private final long refreshTokenExpirationMillis;
    private final User user;



}
