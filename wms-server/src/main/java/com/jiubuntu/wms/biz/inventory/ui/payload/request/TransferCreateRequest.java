package com.jiubuntu.wms.biz.inventory.ui.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransferCreateRequest {

    @NotNull(message = "상품을 선택해주세요.")
    private Long productId;

    @NotNull(message = "출발 위치를 선택해주세요.")
    private Long fromLocationId;

    @NotNull(message = "도착 위치를 선택해주세요.")
    private Long toLocationId;

    private String lotNumber;

    @NotNull(message = "이동 수량을 입력해주세요.")
    @Min(value = 1, message = "이동 수량은 1 이상이어야 합니다.")
    private Integer quantity;

    private Long reasonId;

    private String note;

}
