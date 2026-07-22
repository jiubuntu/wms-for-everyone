package com.jiubuntu.wms.biz.product.ui.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateRequest {

    @NotBlank(message = "상품명을 입력해주세요.")
    @Size(max = 200, message = "상품명은 200자 이내로 입력해주세요.")
    private String name;

    @NotNull(message = "카테고리를 선택해주세요.")
    private Long categoryId;

    @NotNull(message = "보관 유형을 선택해주세요.")
    private Long storageTypeId;

    @NotNull(message = "기본 단위를 선택해주세요.")
    private Long baseUnitId;

    private Long subUnitId;

    private BigDecimal unitConversionRate;

    private boolean lotTracking;

    private String description;

}
