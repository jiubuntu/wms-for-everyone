package com.jiubuntu.wms.biz.inbound.ui.payload.request;

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
public class InboundCreateRequest {

    @NotBlank(message = "공급업체명을 입력해주세요.")
    private String supplierName;

    private String note;

    @NotEmpty(message = "상품을 하나 이상 등록해주세요.")
    @Valid
    private List<InboundItemRequest> items;

}
