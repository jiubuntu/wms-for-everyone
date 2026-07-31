package com.jiubuntu.wms.biz.inbound.infrastructure.custom;

import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundHeaderResult;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundListResult;
import com.jiubuntu.wms.biz.inbound.domain.Inbound;
import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CustomInboundRepository {

    Optional<Inbound> findActiveById(Long id);

    Optional<InboundHeaderResult> findHeaderResultById(Long id);

    Page<InboundListResult> findActiveByWarehouse(Long warehouseId, Pageable pageable);

    long countActiveByWarehouseAndStatus(Long warehouseId, InboundStatus status);

    long countActiveByWarehouseAndStatusAndUpdatedAtBetween(
            Long warehouseId, InboundStatus status, LocalDateTime from, LocalDateTime to);

    List<InboundListResult> findActiveWaitingQueue(Long warehouseId, Collection<InboundStatus> statuses, int limit);

    Map<InboundStatus, Long> countActiveByCompanyAndCreatedAtBetweenGroupByStatus(
            Long companyId, LocalDateTime from, LocalDateTime to);

    Map<Long, Long> countActiveByWarehousesAndStatus(Collection<Long> warehouseIds, InboundStatus status);

    List<LocalDateTime> findActiveUpdatedAtByCompanyAndStatusAndUpdatedAtBetween(
            Long companyId, InboundStatus status, LocalDateTime from, LocalDateTime to);

}
