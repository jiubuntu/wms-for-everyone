package com.jiubuntu.wms.biz.outbound.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AllocationType {

    FEFO("자동"),
    MANUAL("수동"),
    ;

    private final String title;

}
