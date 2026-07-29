package com.jiubuntu.wms.biz.user.ui.payload.request;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserIssueRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "연락처를 입력해주세요.")
    private String phone;

    @NotNull(message = "역할을 선택해주세요.")
    private UserRole role;

    @NotNull(message = "배정할 창고를 선택해주세요.")
    private Long warehouseId;

}
