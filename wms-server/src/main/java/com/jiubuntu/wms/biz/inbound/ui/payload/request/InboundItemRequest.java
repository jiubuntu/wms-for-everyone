package com.jiubuntu.wms.biz.inbound.ui.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class InboundItemRequest {

    @NotNull(message = "상품을 선택해주세요.")
    private Long productId;

    @NotNull(message = "단위를 선택해주세요.")
    private Long unitId;

    @NotNull(message = "입고 수량을 입력해주세요.")
    @Min(value = 1, message = "입고 수량은 1 이상이어야 합니다.")
    private Integer quantity;

    private String lotNumber;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;

}
