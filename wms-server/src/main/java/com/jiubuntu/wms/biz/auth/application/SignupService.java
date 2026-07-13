package com.jiubuntu.wms.biz.auth.application;

import com.jiubuntu.wms.biz.auth.application.dto.command.AuthSignupCommand;
import com.jiubuntu.wms.biz.auth.application.validator.SignupValidator;
import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.user.application.UserService;
import com.jiubuntu.wms.biz.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final SignupValidator signupValidator;
    private final CompanyService companyService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signup(AuthSignupCommand command) {
        signupValidator.validate(command);

        Company company = companyService.create(command.getCompanyName(), command.getBusinessNumber());
        companyService.addBusinessLicenseFile(company, command.getBusinessLicenseFile());

        String encodedPassword = passwordEncoder.encode(command.getPassword());
        return userService.create(company, command.getEmail(), encodedPassword, command.getName(), command.getPhone());
    }

}
