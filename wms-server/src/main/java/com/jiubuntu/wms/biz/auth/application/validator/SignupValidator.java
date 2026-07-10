package com.jiubuntu.wms.biz.auth.application.validator;

import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.user.application.UserService;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignupValidator {

    private final UserService userService;
    private final CompanyService companyService;

    public void validate(String email, String password, String passwordConfirm, String businessNumber) {
        if (!password.equals(passwordConfirm)) {
            throw new CommonException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (userService.existsByEmail(email)) {
            throw new CommonException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (companyService.existsByBusinessNumber(businessNumber)) {
            throw new CommonException(ErrorCode.BUSINESS_NUMBER_ALREADY_EXISTS);
        }
    }

}
