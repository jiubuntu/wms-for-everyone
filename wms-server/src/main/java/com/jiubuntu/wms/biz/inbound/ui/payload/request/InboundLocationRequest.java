package com.jiubuntu.wms.biz.inbound.ui.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InboundLocationRequest {

    @NotNull(message = "위치를 선택해주세요.")
    private Long locationId;

    @NotNull(message = "배치 수량을 입력해주세요.")
    @Min(value = 1, message = "배치 수량은 1 이상이어야 합니다.")
    private Integer quantity;

}
