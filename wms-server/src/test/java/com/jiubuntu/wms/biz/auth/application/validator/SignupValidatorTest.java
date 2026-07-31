package com.jiubuntu.wms.biz.auth.application.validator;

import com.jiubuntu.wms.biz.auth.application.dto.command.AuthSignupCommand;
import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.user.application.UserService;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignupValidatorTest {

    @Mock
    private UserService userService;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private SignupValidator signupValidator;

    private AuthSignupCommand command(String password, String passwordConfirm, MultipartFile file) {
        return new AuthSignupCommand(
                "user@test.com", password, passwordConfirm, "홍길동", "010-0000-0000",
                "테스트기업", "123-45-67890", file
        );
    }

    private MultipartFile validFile() {
        return new MockMultipartFile("businessLicenseFile", "license.pdf", "application/pdf", new byte[]{1, 2, 3});
    }

    @Test
    @DisplayName("비밀번호와 비밀번호 확인이 다르면 PASSWORD_MISMATCH 예외가 발생한다")
    void validate_passwordMismatch() {
        AuthSignupCommand command = command("password1!", "password2!", validFile());

        assertThatThrownBy(() -> signupValidator.validate(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.PASSWORD_MISMATCH);
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 EMAIL_ALREADY_EXISTS 예외가 발생한다")
    void validate_emailAlreadyExists() {
        when(userService.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> signupValidator.validate(command("password1!", "password1!", validFile())))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("이미 등록된 사업자등록번호면 BUSINESS_NUMBER_ALREADY_EXISTS 예외가 발생한다")
    void validate_businessNumberAlreadyExists() {
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(companyService.existsByBusinessNumber(anyString())).thenReturn(true);

        assertThatThrownBy(() -> signupValidator.validate(command("password1!", "password1!", validFile())))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.BUSINESS_NUMBER_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("사업자등록증 파일이 10MB를 초과하면 FILE_TOO_LARGE 예외가 발생한다")
    void validate_fileTooLarge() {
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(companyService.existsByBusinessNumber(anyString())).thenReturn(false);
        MultipartFile oversizedFile = new MockMultipartFile(
                "businessLicenseFile", "license.pdf", "application/pdf", new byte[10 * 1024 * 1024 + 1]
        );

        assertThatThrownBy(() -> signupValidator.validate(command("password1!", "password1!", oversizedFile)))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.FILE_TOO_LARGE);
    }

    @Test
    @DisplayName("지원하지 않는 파일 형식이면 UNSUPPORTED_FILE_TYPE 예외가 발생한다")
    void validate_unsupportedFileType() {
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(companyService.existsByBusinessNumber(anyString())).thenReturn(false);
        MultipartFile invalidTypeFile = new MockMultipartFile(
                "businessLicenseFile", "license.txt", "text/plain", new byte[]{1, 2, 3}
        );

        assertThatThrownBy(() -> signupValidator.validate(command("password1!", "password1!", invalidTypeFile)))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNSUPPORTED_FILE_TYPE);
    }

    @Test
    @DisplayName("모든 조건을 만족하면 예외가 발생하지 않는다")
    void validate_success() {
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(companyService.existsByBusinessNumber(anyString())).thenReturn(false);

        assertThatCode(() -> signupValidator.validate(command("password1!", "password1!", validFile())))
                .doesNotThrowAnyException();
    }

}
