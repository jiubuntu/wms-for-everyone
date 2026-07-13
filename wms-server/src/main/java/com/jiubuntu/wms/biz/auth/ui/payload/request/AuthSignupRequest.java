package com.jiubuntu.wms.biz.auth.ui.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class AuthSignupRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
            message = "비밀번호는 최소 8자 이상, 영문/숫자/특수문자를 모두 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "연락처를 입력해주세요.")
    private String phone;

    @NotBlank(message = "기업명을 입력해주세요.")
    private String companyName;

    @NotBlank(message = "사업자등록번호를 입력해주세요.")
    private String businessNumber;

    @NotNull(message = "사업자등록증 파일을 첨부해주세요.")
    private MultipartFile businessLicenseFile;

}
