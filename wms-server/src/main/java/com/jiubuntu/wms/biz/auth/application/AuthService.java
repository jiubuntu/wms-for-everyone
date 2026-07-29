package com.jiubuntu.wms.biz.auth.application;

import com.jiubuntu.wms.biz.auth.application.dto.result.AuthLoginResult;
import com.jiubuntu.wms.biz.auth.application.validator.AuthValidator;
import com.jiubuntu.wms.global.security.authentication.AuthPrincipal;
import com.jiubuntu.wms.biz.auth.domain.AuthToken;
import com.jiubuntu.wms.biz.auth.domain.AuthTokenType;
import com.jiubuntu.wms.global.security.authentication.JwtProvider;
import com.jiubuntu.wms.biz.auth.infrastructure.AuthTokenRepository;
import com.jiubuntu.wms.biz.user.application.UserService;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserService userService;
    private final AuthTokenRepository authTokenRepository;
    private final JwtProvider jwtProvider;
    private final AuthValidator authValidator;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${security.login.max-fail-count}")
    private int maxFailCount;

    @Value("${security.login.lock-duration-minutes}")
    private long lockDurationMinutes;

    @Transactional
    public AuthLoginResult login(String email, String password) {
        User user = userService.findActiveByEmail(email)
                .orElseThrow(() -> new CommonException(ErrorCode.INVALID_CREDENTIALS));

        try {
            authValidator.validateLogin(user, password);
        } catch (CommonException e) {
            if (e.getErrorCode() == ErrorCode.INVALID_CREDENTIALS) {
                user.increaseLoginFailedCount();
                if (user.getLoginFailedCount() >= maxFailCount) {
                    user.lock(LocalDateTime.now().plusMinutes(lockDurationMinutes));
                }
            }
            throw e;
        }

        user.resetLoginFailedCount();

        return issueTokens(user);
    }

    @Transactional
    public AuthLoginResult refresh(String refreshTokenValue) {
        if (refreshTokenValue == null) {
            throw new CommonException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        AuthToken refreshToken = authTokenRepository.findActiveByTokenAndType(refreshTokenValue, AuthTokenType.REFRESH)
                .orElseThrow(() -> new CommonException(ErrorCode.INVALID_REFRESH_TOKEN));

        authValidator.validateRefreshToken(refreshToken);

        refreshToken.delete();

        return issueTokens(refreshToken.getUser());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        authTokenRepository.findActiveByTokenAndType(refreshTokenValue, AuthTokenType.REFRESH)
                .ifPresent(AuthToken::delete);
    }

    private AuthLoginResult issueTokens(User user) {
        Long warehouseId = user.getWarehouse() != null ? user.getWarehouse().getId() : null;
        AuthPrincipal principal = new AuthPrincipal(
                user.getId(), user.getCompany().getId(), user.getRole(), warehouseId, user.isMustChangePassword());
        String accessToken = jwtProvider.generateAccessToken(principal);

        String refreshTokenValue = UUID.randomUUID().toString();
        LocalDateTime expiredAt = LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpiration));
        AuthToken refreshToken = new AuthToken(user, refreshTokenValue, AuthTokenType.REFRESH, expiredAt);
        authTokenRepository.save(refreshToken);

        return new AuthLoginResult(accessToken, refreshTokenValue, refreshTokenExpiration, user);
    }

}
