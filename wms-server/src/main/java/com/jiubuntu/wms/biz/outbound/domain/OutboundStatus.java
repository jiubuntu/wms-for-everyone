package com.jiubuntu.wms.biz.outbound.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutboundStatus {

    PENDING("대기"),
    PICKING("피킹중"),
    COMPLETED("확정"),
    CANCELLED("취소"),
    ;

    private final String title;

}
