package com.jiubuntu.wms.biz.outbound.infrastructure.custom;

import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundAllocationRow;

import java.util.List;

public interface CustomOutboundItemLocationRepository {

    List<OutboundAllocationRow> findActiveRowsByOutboundItemIdIn(List<Long> outboundItemIds);

}
