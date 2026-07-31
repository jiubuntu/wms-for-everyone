package com.jiubuntu.wms.biz.user.application.validator;

import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserChangePasswordValidator {

    private final PasswordEncoder passwordEncoder;

    public void validate(User user, String currentPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new CommonException(ErrorCode.INVALID_PASSWORD);
        }
    }

}
