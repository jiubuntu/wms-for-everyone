package com.jiubuntu.wms.biz.auth.application;

import com.jiubuntu.wms.biz.auth.application.dto.command.AuthSignupCommand;
import com.jiubuntu.wms.biz.auth.application.validator.SignupValidator;
import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.user.application.UserService;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private SignupValidator signupValidator;

    @Mock
    private CompanyService companyService;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignupService signupService;

    @Test
    @DisplayName("회원가입 시 검증 후 기업 생성, 사업자등록증 업로드, 계정 생성을 순서대로 위임한다")
    void signup_success() {
        MultipartFile file = new MockMultipartFile("businessLicenseFile", "license.pdf", "application/pdf", new byte[]{1, 2, 3});
        AuthSignupCommand command = new AuthSignupCommand(
                "user@test.com", "password1!", "password1!", "홍길동", "010-0000-0000",
                "테스트기업", "123-45-67890", file
        );
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.PENDING);
        User createdUser = new User(company, null, "user@test.com", "encoded-password",
                "홍길동", "010-0000-0000", UserRole.COMPANY_ADMIN, UserStatus.PENDING);

        when(companyService.create("테스트기업", "123-45-67890")).thenReturn(company);
        when(passwordEncoder.encode("password1!")).thenReturn("encoded-password");
        when(userService.create(company, "user@test.com", "encoded-password", "홍길동", "010-0000-0000"))
                .thenReturn(createdUser);

        User result = signupService.signup(command);

        assertThat(result).isEqualTo(createdUser);
        verify(signupValidator).validate(command);
        verify(companyService).addBusinessLicenseFile(eq(company), any(MultipartFile.class));
    }

}
