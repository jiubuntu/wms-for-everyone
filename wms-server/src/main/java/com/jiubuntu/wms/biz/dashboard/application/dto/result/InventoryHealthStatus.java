package com.jiubuntu.wms.biz.dashboard.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InventoryHealthStatus {

    NORMAL("정상"),
    LOW("부족"),
    UNAVAILABLE("가용없음"),
    ;

    private final String title;

}
