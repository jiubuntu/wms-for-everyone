package com.jiubuntu.wms.biz.inbound.ui.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InboundLocationReplaceRequest {

    @NotEmpty(message = "위치를 하나 이상 배치해주세요.")
    @Valid
    private List<InboundLocationRequest> locations;

}
