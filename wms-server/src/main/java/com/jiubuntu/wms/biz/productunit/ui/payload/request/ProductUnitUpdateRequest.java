package com.jiubuntu.wms.biz.productunit.ui.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductUnitUpdateRequest {

    @NotBlank(message = "단위명을 입력해주세요.")
    @Size(max = 50, message = "단위명은 50자 이내로 입력해주세요.")
    private String name;

}
