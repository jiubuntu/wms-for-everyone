package com.jiubuntu.wms.biz.inbound.infrastructure.custom;

import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundItemRow;

import java.util.List;

public interface CustomInboundItemRepository {

    List<InboundItemRow> findActiveRowsByInboundId(Long inboundId);

}
