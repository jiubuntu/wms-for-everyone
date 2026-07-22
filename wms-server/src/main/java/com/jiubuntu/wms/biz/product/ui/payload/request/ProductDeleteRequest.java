package com.jiubuntu.wms.biz.product.ui.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductDeleteRequest {

    @NotNull(message = "삭제할 상품을 선택해주세요.")
    private Long id;

}
