package com.jiubuntu.wms.biz.inventory.ui.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryAdjustRequest {

    @NotNull(message = "조정 후 수량을 입력해주세요.")
    @Min(value = 0, message = "수량은 0 이상이어야 합니다.")
    private Integer quantity;

    @NotBlank(message = "조정 사유를 입력해주세요.")
    private String reason;

}
