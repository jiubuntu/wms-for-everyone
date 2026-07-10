package com.jiubuntu.wms.biz.auth.application;

import com.jiubuntu.wms.biz.auth.application.validator.SignupValidator;
import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.user.application.UserService;
import com.jiubuntu.wms.biz.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final SignupValidator signupValidator;
    private final CompanyService companyService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signup(String email, String password, String passwordConfirm, String name, String phone,
                        String companyName, String businessNumber, MultipartFile businessLicenseFile) {
        signupValidator.validate(email, password, passwordConfirm, businessNumber);

        Company company = companyService.create(companyName, businessNumber);
        companyService.addBusinessLicenseFile(company, businessLicenseFile);

        String encodedPassword = passwordEncoder.encode(password);
        return userService.create(company, email, encodedPassword, name, phone);
    }

}
