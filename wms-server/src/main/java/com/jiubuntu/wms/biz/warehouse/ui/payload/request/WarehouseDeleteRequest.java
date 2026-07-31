package com.jiubuntu.wms.biz.warehouse.ui.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WarehouseDeleteRequest {

    @NotNull(message = "창고 ID를 입력해주세요.")
    private Long id;

}
