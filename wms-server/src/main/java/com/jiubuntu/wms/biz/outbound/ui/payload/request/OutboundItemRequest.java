package com.jiubuntu.wms.biz.outbound.ui.payload.request;

import com.jiubuntu.wms.biz.outbound.domain.AllocationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OutboundItemRequest {

    @NotNull(message = "상품을 선택해주세요.")
    private Long productId;

    @NotNull(message = "단위를 선택해주세요.")
    private Long unitId;

    @NotNull(message = "출고 수량을 입력해주세요.")
    @Min(value = 1, message = "출고 수량은 1 이상이어야 합니다.")
    private Integer quantity;

    @NotNull(message = "할당 방식을 선택해주세요.")
    private AllocationType allocationType;

    @Valid
    private List<OutboundAllocationRequest> allocations;

}
