package com.jiubuntu.wms.biz.outbound.application;

import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.outbound.domain.OutboundItem;
import lombok.Getter;

/**
 * register() 안에서 계획(OutboundAllocationPlan)에 상품 라인(OutboundItem)을 붙여,
 * location_id 오름차순 정렬 후 실제 예약 반영에 쓰일 형태로 들고 있는 값 객체.
 */
@Getter
public class OutboundPendingReservation {

    private final OutboundItem item;
    private final Location location;
    private final String lotNumber;
    private final Inventory inventory;
    private final int quantity;

    public OutboundPendingReservation(OutboundItem item, Location location, String lotNumber, Inventory inventory, int quantity) {
        this.item = item;
        this.location = location;
        this.lotNumber = lotNumber;
        this.inventory = inventory;
        this.quantity = quantity;
    }

}
