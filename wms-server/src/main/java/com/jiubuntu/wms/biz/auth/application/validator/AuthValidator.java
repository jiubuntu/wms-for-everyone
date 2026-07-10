package com.jiubuntu.wms.biz.auth.application.validator;

import com.jiubuntu.wms.biz.auth.domain.RefreshToken;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final PasswordEncoder passwordEncoder;

    public void validateLogin(User user, String rawPassword) {
        validateLoginable(user);
        validatePassword(user, rawPassword);
    }

    public void validateRefreshToken(RefreshToken refreshToken) {
        if (!refreshToken.isValid()) {
            throw new CommonException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }
    }

    private void validateLoginable(User user) {
        if (user.getStatus() == UserStatus.LOCKED) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new CommonException(ErrorCode.ACCOUNT_LOCKED);
            }
            user.unlock();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new CommonException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
    }

    private void validatePassword(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CommonException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

}
