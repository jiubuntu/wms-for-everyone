package com.jiubuntu.wms.biz.inventory.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InventoryHistoryTargetType {

    INBOUND("입고"),
    OUTBOUND("출고"),
    TRANSFER("이동"),
    ADJUSTMENT("조정");

    private final String name;

}
