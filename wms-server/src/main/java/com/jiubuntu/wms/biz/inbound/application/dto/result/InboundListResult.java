package com.jiubuntu.wms.biz.inbound.application.dto.result;

import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InboundListResult {

    private final Long id;
    private final String supplierName;
    private final InboundStatus status;
    private final Long itemCount;
    private final LocalDateTime createdAt;

}
