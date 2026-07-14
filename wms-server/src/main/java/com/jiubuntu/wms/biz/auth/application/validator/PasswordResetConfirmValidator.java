package com.jiubuntu.wms.biz.auth.application.validator;

import com.jiubuntu.wms.biz.auth.application.dto.command.AuthPasswordResetConfirmCommand;
import com.jiubuntu.wms.biz.auth.domain.AuthToken;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetConfirmValidator {

    public void validate(AuthToken resetToken, AuthPasswordResetConfirmCommand command) {
        if (!command.getNewPassword().equals(command.getNewPasswordConfirm())) {
            throw new CommonException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (!resetToken.isValid()) {
            throw new CommonException(ErrorCode.EXPIRED_PASSWORD_RESET_TOKEN);
        }
    }

}
