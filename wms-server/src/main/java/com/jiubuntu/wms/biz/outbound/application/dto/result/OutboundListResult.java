package com.jiubuntu.wms.biz.outbound.application.dto.result;

import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OutboundListResult {

    private final Long id;
    private final String customerName;
    private final OutboundStatus status;
    private final Long itemCount;
    private final LocalDateTime createdAt;

}
