package com.jiubuntu.wms.biz.dashboard.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WarehouseOpsRowResult {

    private final Long warehouseId;
    private final String warehouseName;
    private final String warehouseNote;
    private final long outboundPending;
    private final long outboundPicking;
    private final long inboundPending;
    private final long expiringSoonCount;
    private final WarehouseOpsStatus status;

}
