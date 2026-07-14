package com.jiubuntu.wms.biz.auth.application.validator;

import com.jiubuntu.wms.biz.auth.domain.AuthToken;
import com.jiubuntu.wms.biz.auth.domain.AuthTokenType;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthValidatorTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthValidator authValidator = new AuthValidator(passwordEncoder);

    private User activeUser(String rawPassword) {
        return new User(null, null, "user@test.com", passwordEncoder.encode(rawPassword),
                "홍길동", "010-0000-0000", UserRole.COMPANY_ADMIN, UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("활성 계정 + 올바른 비밀번호면 예외가 발생하지 않는다")
    void validateLogin_success() {
        User user = activeUser("password1!");

        authValidator.validateLogin(user, "password1!");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 INVALID_CREDENTIALS 예외가 발생한다")
    void validateLogin_wrongPassword() {
        User user = activeUser("password1!");

        assertThatThrownBy(() -> authValidator.validateLogin(user, "wrong-password"))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("잠금 해제 시각이 지나지 않은 잠금 계정은 ACCOUNT_LOCKED 예외가 발생한다")
    void validateLogin_stillLocked() {
        User user = activeUser("password1!");
        user.lock(LocalDateTime.now().plusMinutes(10));

        assertThatThrownBy(() -> authValidator.validateLogin(user, "password1!"))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_LOCKED);
    }

    @Test
    @DisplayName("잠금 해제 시각이 지난 계정은 자동으로 잠금 해제되어 로그인이 성공한다")
    void validateLogin_lockExpired_autoUnlock() {
        User user = activeUser("password1!");
        user.lock(LocalDateTime.now().minusMinutes(1));

        authValidator.validateLogin(user, "password1!");

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("활성 상태가 아닌 계정(PENDING 등)은 ACCOUNT_NOT_ACTIVE 예외가 발생한다")
    void validateLogin_notActive() {
        User user = new User(null, null, "user@test.com", passwordEncoder.encode("password1!"),
                "홍길동", "010-0000-0000", UserRole.COMPANY_ADMIN, UserStatus.PENDING);

        assertThatThrownBy(() -> authValidator.validateLogin(user, "password1!"))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_NOT_ACTIVE);
    }

    @Test
    @DisplayName("만료되지 않은 리프레시 토큰은 예외가 발생하지 않는다")
    void validateRefreshToken_valid() {
        AuthToken refreshToken = new AuthToken(null, "token", AuthTokenType.REFRESH, LocalDateTime.now().plusDays(1));

        authValidator.validateRefreshToken(refreshToken);
    }

    @Test
    @DisplayName("만료된 리프레시 토큰은 EXPIRED_REFRESH_TOKEN 예외가 발생한다")
    void validateRefreshToken_expired() {
        AuthToken refreshToken = new AuthToken(null, "token", AuthTokenType.REFRESH, LocalDateTime.now().minusSeconds(1));

        assertThatThrownBy(() -> authValidator.validateRefreshToken(refreshToken))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }

}
