package com.jiubuntu.wms.biz.commoncode.ui.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommonCodeUpdateRequest {

    @NotBlank(message = "코드명을 입력해주세요.")
    @Size(max = 100, message = "코드명은 100자 이내로 입력해주세요.")
    private String name;

    private int sortOrder;

}
