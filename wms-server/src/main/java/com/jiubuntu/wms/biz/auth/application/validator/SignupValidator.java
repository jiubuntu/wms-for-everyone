package com.jiubuntu.wms.biz.auth.application.validator;

import com.jiubuntu.wms.biz.auth.application.dto.command.AuthSignupCommand;
import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.user.application.UserService;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SignupValidator {

    private static final long MAX_BUSINESS_LICENSE_FILE_SIZE = 10 * 1024 * 1024;
    private static final List<String> ALLOWED_BUSINESS_LICENSE_CONTENT_TYPES =
            List.of("application/pdf", "image/jpeg", "image/png");

    private final UserService userService;
    private final CompanyService companyService;

    public void validate(AuthSignupCommand command) {
        if (!command.getPassword().equals(command.getPasswordConfirm())) {
            throw new CommonException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (userService.existsByEmail(command.getEmail())) {
            throw new CommonException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (companyService.existsByBusinessNumber(command.getBusinessNumber())) {
            throw new CommonException(ErrorCode.BUSINESS_NUMBER_ALREADY_EXISTS);
        }

        validateBusinessLicenseFile(command.getBusinessLicenseFile());
    }

    private void validateBusinessLicenseFile(MultipartFile businessLicenseFile) {
        if (businessLicenseFile.getSize() > MAX_BUSINESS_LICENSE_FILE_SIZE) {
            throw new CommonException(ErrorCode.FILE_TOO_LARGE);
        }

        if (!ALLOWED_BUSINESS_LICENSE_CONTENT_TYPES.contains(businessLicenseFile.getContentType())) {
            throw new CommonException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }
    }

}
