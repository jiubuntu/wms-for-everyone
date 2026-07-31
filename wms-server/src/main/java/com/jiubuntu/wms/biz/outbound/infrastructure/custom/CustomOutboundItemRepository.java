package com.jiubuntu.wms.biz.outbound.infrastructure.custom;

import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundItemRow;

import java.util.List;

public interface CustomOutboundItemRepository {

    List<OutboundItemRow> findActiveRowsByOutboundId(Long outboundId);

}
