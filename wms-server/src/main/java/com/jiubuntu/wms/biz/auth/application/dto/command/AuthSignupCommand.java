package com.jiubuntu.wms.biz.auth.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class AuthSignupCommand {

    private final String email;
    private final String password;
    private final String passwordConfirm;
    private final String name;
    private final String phone;
    private final String companyName;
    private final String businessNumber;
    private final MultipartFile businessLicenseFile;



}
