package com.jiubuntu.wms.biz.commoncode.ui.payload.request;

import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommonCodeCreateRequest {

    @NotNull(message = "코드 그룹을 선택해주세요.")
    private CommonCodeGroup groupCode;

    @NotBlank(message = "코드를 입력해주세요.")
    @Size(max = 50, message = "코드는 50자 이내로 입력해주세요.")
    private String code;

    @NotBlank(message = "코드명을 입력해주세요.")
    @Size(max = 100, message = "코드명은 100자 이내로 입력해주세요.")
    private String name;

    private int sortOrder;

}
