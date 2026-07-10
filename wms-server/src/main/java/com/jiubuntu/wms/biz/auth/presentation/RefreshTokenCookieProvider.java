package com.jiubuntu.wms.biz.auth.presentation;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieProvider {

    public static final String COOKIE_NAME = "refreshToken";

    private static final String COOKIE_PATH = "/api/auth";

    public ResponseCookie issue(String value, long maxAgeMillis) {
        return build(value, maxAgeMillis / 1000);
    }

    public ResponseCookie expire() {
        return build("", 0);
    }

    private ResponseCookie build(String value, long maxAgeSeconds) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .build();
    }

}
