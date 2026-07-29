package com.jiubuntu.wms.biz.warehouse.ui.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WarehouseCreateRequest {

    @NotBlank(message = "창고명을 입력해주세요.")
    @Size(max = 100, message = "창고명은 100자 이내로 입력해주세요.")
    private String name;

    @Size(max = 255, message = "주소는 255자 이내로 입력해주세요.")
    private String address;

}
