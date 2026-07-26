package com.jiubuntu.wms.biz.outbound.ui.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OutboundCreateRequest {

    @NotBlank(message = "고객사명을 입력해주세요.")
    private String customerName;

    private String note;

    @NotEmpty(message = "상품을 하나 이상 등록해주세요.")
    @Valid
    private List<OutboundItemRequest> items;

}
