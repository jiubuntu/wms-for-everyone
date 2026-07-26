package com.jiubuntu.wms.biz.outbound.application;

import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.location.domain.Location;
import lombok.Getter;

/**
 * OutboundAllocationPlanner가 계산한 "이 상품 라인을 어느 위치·LOT에서 얼마나 뺄지" 계획 한 건.
 * OutboundService가 이 계획을 location_id 오름차순으로 정렬해 실제 예약(reserve)에 반영한다.
 */
@Getter
public class OutboundAllocationPlan {

    private final Location location;
    private final String lotNumber;
    private final Inventory inventory;
    private final int quantity;

    public OutboundAllocationPlan(Location location, String lotNumber, Inventory inventory, int quantity) {
        this.location = location;
        this.lotNumber = lotNumber;
        this.inventory = inventory;
        this.quantity = quantity;
    }

}
