package com.jiubuntu.wms.biz.productunit.ui.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductUnitDeleteRequest {

    @NotNull(message = "삭제할 단위를 선택해주세요.")
    private Long id;

}
