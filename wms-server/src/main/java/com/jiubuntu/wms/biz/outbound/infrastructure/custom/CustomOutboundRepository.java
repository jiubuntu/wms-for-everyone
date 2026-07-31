package com.jiubuntu.wms.biz.outbound.infrastructure.custom;

import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundHeaderResult;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundListResult;
import com.jiubuntu.wms.biz.outbound.domain.Outbound;
import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CustomOutboundRepository {

    Optional<Outbound> findActiveById(Long id);

    Optional<OutboundHeaderResult> findHeaderResultById(Long id);

    Page<OutboundListResult> findActiveByWarehouse(Long warehouseId, Pageable pageable);

    long countActiveByWarehouseAndStatus(Long warehouseId, OutboundStatus status);

    long countActiveByWarehouseAndStatusAndUpdatedAtBetween(
            Long warehouseId, OutboundStatus status, LocalDateTime from, LocalDateTime to);

    List<OutboundListResult> findActiveWaitingQueue(Long warehouseId, Collection<OutboundStatus> statuses, int limit);

    Map<OutboundStatus, Long> countActiveByCompanyAndCreatedAtBetweenGroupByStatus(
            Long companyId, LocalDateTime from, LocalDateTime to);

    Map<Long, Long> countActiveByWarehousesAndStatus(Collection<Long> warehouseIds, OutboundStatus status);

    List<LocalDateTime> findActiveUpdatedAtByCompanyAndStatusAndUpdatedAtBetween(
            Long companyId, OutboundStatus status, LocalDateTime from, LocalDateTime to);

}
