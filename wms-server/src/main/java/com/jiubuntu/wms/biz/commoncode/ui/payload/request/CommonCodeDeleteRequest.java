package com.jiubuntu.wms.biz.commoncode.ui.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommonCodeDeleteRequest {

    @NotNull(message = "삭제할 코드를 선택해주세요.")
    private Long id;

}
