package com.jiubuntu.wms.biz.auth.ui;

import com.jiubuntu.wms.biz.auth.application.AuthService;
import com.jiubuntu.wms.biz.auth.application.PasswordResetService;
import com.jiubuntu.wms.biz.auth.application.SignupService;
import com.jiubuntu.wms.biz.auth.application.dto.command.AuthPasswordResetConfirmCommand;
import com.jiubuntu.wms.biz.auth.application.dto.result.AuthLoginResult;
import com.jiubuntu.wms.biz.auth.application.dto.command.AuthSignupCommand;
import com.jiubuntu.wms.biz.auth.ui.payload.request.AuthLoginRequest;
import com.jiubuntu.wms.biz.auth.ui.payload.request.AuthPasswordResetConfirmRequest;
import com.jiubuntu.wms.biz.auth.ui.payload.request.AuthPasswordResetMailRequest;
import com.jiubuntu.wms.biz.auth.ui.payload.response.AuthLoginResponse;
import com.jiubuntu.wms.biz.auth.ui.payload.response.AuthRefreshResponse;
import com.jiubuntu.wms.biz.auth.ui.payload.request.AuthSignupRequest;
import com.jiubuntu.wms.biz.auth.ui.payload.response.AuthSignupResponse;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.global.payload.constants.ResponseCode;
import com.jiubuntu.wms.global.payload.response.ApiCommonResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
    private final PasswordResetService passwordResetService;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    @PostMapping("/signup")
    public ResponseEntity<ApiCommonResponse<AuthSignupResponse>> signup(
            @Valid @ModelAttribute AuthSignupRequest request
    ) {
        AuthSignupCommand command = new AuthSignupCommand(
                request.getEmail(),
                request.getPassword(),
                request.getPasswordConfirm(),
                request.getName(),
                request.getPhone(),
                request.getCompanyName(),
                request.getBusinessNumber(),
                request.getBusinessLicenseFile()
        );
        User user = signupService.signup(command);

        ApiCommonResponse<AuthSignupResponse> response = ApiCommonResponse.success(AuthSignupResponse.from(user), ResponseCode.CREATED);
        return ResponseEntity.status(response.getHttpCode()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiCommonResponse<AuthLoginResponse>> login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletResponse response
    ) {
        AuthLoginResult result = authService.login(request.getEmail(), request.getPassword());

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.issue(result.getRefreshToken(), result.getRefreshTokenExpirationMillis()).toString());

        ApiCommonResponse<AuthLoginResponse> body = ApiCommonResponse.success(AuthLoginResponse.of(result.getAccessToken(), result.getUser()));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiCommonResponse<AuthRefreshResponse>> refresh(
            @CookieValue(value = RefreshTokenCookieProvider.COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        AuthLoginResult result = authService.refresh(refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.issue(result.getRefreshToken(), result.getRefreshTokenExpirationMillis()).toString());

        ApiCommonResponse<AuthRefreshResponse> body = ApiCommonResponse.success(AuthRefreshResponse.of(result.getAccessToken()));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiCommonResponse<Void>> logout(
            @CookieValue(value = RefreshTokenCookieProvider.COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.expire().toString());

        ApiCommonResponse<Void> body = ApiCommonResponse.success(null);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<ApiCommonResponse<Void>> requestPasswordReset(
            @Valid @RequestBody AuthPasswordResetMailRequest request
    ) {
        passwordResetService.requestReset(request.getEmail());

        ApiCommonResponse<Void> body = ApiCommonResponse.success(null);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/password/reset-confirm")
    public ResponseEntity<ApiCommonResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody AuthPasswordResetConfirmRequest request
    ) {
        AuthPasswordResetConfirmCommand command = new AuthPasswordResetConfirmCommand(
                request.getToken(), request.getNewPassword(), request.getNewPasswordConfirm()
        );
        passwordResetService.confirmReset(command);

        ApiCommonResponse<Void> body = ApiCommonResponse.success(null);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
