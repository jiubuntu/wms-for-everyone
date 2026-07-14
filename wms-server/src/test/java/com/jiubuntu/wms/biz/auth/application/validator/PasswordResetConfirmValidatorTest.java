package com.jiubuntu.wms.biz.auth.application.validator;

import com.jiubuntu.wms.biz.auth.application.dto.command.AuthPasswordResetConfirmCommand;
import com.jiubuntu.wms.biz.auth.domain.AuthToken;
import com.jiubuntu.wms.biz.auth.domain.AuthTokenType;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordResetConfirmValidatorTest {

    private final PasswordResetConfirmValidator validator = new PasswordResetConfirmValidator();

    @Test
    @DisplayName("새 비밀번호와 확인이 일치하고 토큰이 유효하면 예외가 발생하지 않는다")
    void validate_success() {
        AuthToken token = new AuthToken(null, "token", AuthTokenType.PASSWORD_RESET, LocalDateTime.now().plusMinutes(30));
        AuthPasswordResetConfirmCommand command = new AuthPasswordResetConfirmCommand("token", "password1!", "password1!");

        assertThatCode(() -> validator.validate(token, command)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("새 비밀번호와 확인이 다르면 PASSWORD_MISMATCH 예외가 발생한다")
    void validate_passwordMismatch() {
        AuthToken token = new AuthToken(null, "token", AuthTokenType.PASSWORD_RESET, LocalDateTime.now().plusMinutes(30));
        AuthPasswordResetConfirmCommand command = new AuthPasswordResetConfirmCommand("token", "password1!", "password2!");

        assertThatThrownBy(() -> validator.validate(token, command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.PASSWORD_MISMATCH);
    }

    @Test
    @DisplayName("토큰이 만료됐으면 EXPIRED_PASSWORD_RESET_TOKEN 예외가 발생한다")
    void validate_expiredToken() {
        AuthToken token = new AuthToken(null, "token", AuthTokenType.PASSWORD_RESET, LocalDateTime.now().minusSeconds(1));
        AuthPasswordResetConfirmCommand command = new AuthPasswordResetConfirmCommand("token", "password1!", "password1!");

        assertThatThrownBy(() -> validator.validate(token, command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.EXPIRED_PASSWORD_RESET_TOKEN);
    }

}
