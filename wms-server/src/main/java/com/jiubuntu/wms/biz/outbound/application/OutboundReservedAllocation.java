package com.jiubuntu.wms.biz.outbound.application;

import com.jiubuntu.wms.biz.location.domain.Location;
import lombok.Getter;

/**
 * complete()/cancel()에서 이미 확정된(등록 시점에 예약된) 할당 내역을 다시 조회한 결과를 담는 값 객체.
 */
@Getter
public class OutboundReservedAllocation {

    private final Location location;
    private final String lotNumber;
    private final Long productId;
    private final int quantity;

    public OutboundReservedAllocation(Location location, String lotNumber, Long productId, int quantity) {
        this.location = location;
        this.lotNumber = lotNumber;
        this.productId = productId;
        this.quantity = quantity;
    }

}
