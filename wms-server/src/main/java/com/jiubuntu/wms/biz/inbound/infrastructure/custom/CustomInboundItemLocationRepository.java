package com.jiubuntu.wms.biz.inbound.infrastructure.custom;

import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundLocationRow;

import java.util.List;

public interface CustomInboundItemLocationRepository {

    List<InboundLocationRow> findActiveRowsByInboundItemIdIn(List<Long> inboundItemIds);

}
