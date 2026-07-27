package com.jiubuntu.wms.biz.inbound.infrastructure.custom;

import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundHeaderResult;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundListResult;
import com.jiubuntu.wms.biz.inbound.domain.Inbound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomInboundRepository {

    Optional<Inbound> findActiveById(Long id);

    Optional<InboundHeaderResult> findHeaderResultById(Long id);

    Page<InboundListResult> findActiveByWarehouse(Long warehouseId, Pageable pageable);

}
