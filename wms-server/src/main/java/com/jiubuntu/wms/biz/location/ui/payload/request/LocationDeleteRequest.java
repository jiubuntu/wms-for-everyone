package com.jiubuntu.wms.biz.location.ui.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LocationDeleteRequest {

    @NotNull(message = "위치 ID를 입력해주세요.")
    private Long id;

}
