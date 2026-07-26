package com.jiubuntu.wms.biz.outbound.infrastructure.custom;

import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundHeaderResult;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundListResult;
import com.jiubuntu.wms.biz.outbound.domain.Outbound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomOutboundRepository {

    Optional<Outbound> findActiveById(Long id);

    Optional<OutboundHeaderResult> findHeaderResultById(Long id);

    Page<OutboundListResult> findActiveByWarehouse(Long warehouseId, Pageable pageable);

}
