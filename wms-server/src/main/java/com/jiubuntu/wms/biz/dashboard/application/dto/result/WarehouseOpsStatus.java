package com.jiubuntu.wms.biz.dashboard.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WarehouseOpsStatus {

    NORMAL("정상"),
    WATCH("관찰"),
    ALERT("주의"),
    ;

    private final String title;

}
