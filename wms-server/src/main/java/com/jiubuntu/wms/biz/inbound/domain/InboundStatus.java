package com.jiubuntu.wms.biz.inbound.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InboundStatus {

    PENDING("대기"),
    IN_PROGRESS("위치배치중"),
    COMPLETED("확정"),
    CANCELLED("취소"),
    ;

    private final String title;

}
