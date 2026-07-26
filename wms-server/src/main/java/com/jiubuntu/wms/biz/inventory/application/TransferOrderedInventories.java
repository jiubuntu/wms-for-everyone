package com.jiubuntu.wms.biz.inventory.application;

import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import lombok.Getter;

/**
 * 재고 이동(Transfer) 확정 시 location_id 오름차순으로 갱신을 마친 출발/도착 재고를 담는 값 객체.
 */
@Getter
public class TransferOrderedInventories {

    private final Inventory from;
    private final Inventory to;

    public TransferOrderedInventories(Inventory from, Inventory to) {
        this.from = from;
        this.to = to;
    }

}
