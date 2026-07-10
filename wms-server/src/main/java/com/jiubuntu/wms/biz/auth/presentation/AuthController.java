package com.jiubuntu.wms.biz.auth.presentation;

import com.jiubuntu.wms.biz.auth.application.AuthService;
import com.jiubuntu.wms.biz.auth.application.SignupService;
import com.jiubuntu.wms.biz.auth.application.dto.LoginResult;
import com.jiubuntu.wms.biz.auth.presentation.payload.AuthLoginRequest;
import com.jiubuntu.wms.biz.auth.presentation.payload.AuthLoginResponse;
import com.jiubuntu.wms.biz.auth.presentation.payload.AuthRefreshResponse;
import com.jiubuntu.wms.biz.auth.presentation.payload.AuthSignupRequest;
import com.jiubuntu.wms.biz.auth.presentation.payload.AuthSignupResponse;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.global.payload.constants.ResponseCode;
import com.jiubuntu.wms.global.payload.response.ApiCommonResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignupService signupService;
    private final AuthService authService;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    @PostMapping("/signup")
    public ApiCommonResponse<AuthSignupResponse> signup(
            @Valid @ModelAttribute AuthSignupRequest request
    ) {
        User user = signupService.signup(
                request.getEmail(),
                request.getPassword(),
                request.getPasswordConfirm(),
                request.getName(),
                request.getPhone(),
                request.getCompanyName(),
                request.getBusinessNumber(),
                request.getBusinessLicenseFile()
        );

        return ApiCommonResponse.success(AuthSignupResponse.from(user), ResponseCode.CREATED);
    }

    @PostMapping("/login")
    public ApiCommonResponse<AuthLoginResponse> login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletResponse response
    ) {
        LoginResult result = authService.login(request.getEmail(), request.getPassword());

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.issue(result.getRefreshToken(), result.getRefreshTokenExpirationMillis()).toString());

        return ApiCommonResponse.success(AuthLoginResponse.of(result.getAccessToken(), result.getUser()));
    }

    @PostMapping("/refresh")
    public ApiCommonResponse<AuthRefreshResponse> refresh(
            @CookieValue(value = RefreshTokenCookieProvider.COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        LoginResult result = authService.refresh(refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.issue(result.getRefreshToken(), result.getRefreshTokenExpirationMillis()).toString());

        return ApiCommonResponse.success(AuthRefreshResponse.of(result.getAccessToken()));
    }

    @PostMapping("/logout")
    public ApiCommonResponse<Void> logout(
            @CookieValue(value = RefreshTokenCookieProvider.COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.expire().toString());

        return ApiCommonResponse.success(null);
    }

}
