package com.jiubuntu.wms.biz.outbound.infrastructure.custom;

import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundHeaderResult;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundListResult;
import com.jiubuntu.wms.biz.outbound.domain.Outbound;
import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import com.jiubuntu.wms.biz.outbound.domain.QOutbound;
import com.jiubuntu.wms.biz.outbound.domain.QOutboundItem;
import com.jiubuntu.wms.biz.user.domain.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomOutboundRepositoryImpl implements CustomOutboundRepository {

    private static final QOutbound outbound = QOutbound.outbound;
    private static final QOutboundItem outboundItem = QOutboundItem.outboundItem;
    private static final QUser processedByUser = new QUser("outboundProcessedByUser");

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Outbound> findActiveById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(outbound)
                        .where(outbound.id.eq(id), activeEq())
                        .fetchOne()
        );
    }

    @Override
    public Optional<OutboundHeaderResult> findHeaderResultById(Long id) {
        OutboundHeaderResult result = queryFactory
                .select(Projections.constructor(
                        OutboundHeaderResult.class,
                        outbound.id,
                        outbound.warehouse.id,
                        outbound.customerName,
                        outbound.status,
                        outbound.note,
                        processedByUser.name,
                        outbound.createdAt
                ))
                .from(outbound)
                .leftJoin(processedByUser).on(processedByUser.id.eq(outbound.processedBy))
                .where(outbound.id.eq(id), activeEq())
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Page<OutboundListResult> findActiveByWarehouse(Long warehouseId, Pageable pageable) {
        List<OutboundListResult> content = queryFactory
                .select(Projections.constructor(
                        OutboundListResult.class,
                        outbound.id,
                        outbound.customerName,
                        outbound.status,
                        JPAExpressions.select(outboundItem.count())
                                .from(outboundItem)
                                .where(outboundItem.outbound.id.eq(outbound.id), outboundItem.active.isTrue()),
                        outbound.createdAt
                ))
                .from(outbound)
                .where(outbound.warehouse.id.eq(warehouseId), activeEq())
                .orderBy(outbound.createdAt.desc(), outbound.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(outbound.count())
                .from(outbound)
                .where(outbound.warehouse.id.eq(warehouseId), activeEq())
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public long countActiveByWarehouseAndStatus(Long warehouseId, OutboundStatus status) {
        Long count = queryFactory
                .select(outbound.count())
                .from(outbound)
                .where(outbound.warehouse.id.eq(warehouseId), outbound.status.eq(status), activeEq())
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public long countActiveByWarehouseAndStatusAndUpdatedAtBetween(
            Long warehouseId, OutboundStatus status, LocalDateTime from, LocalDateTime to) {
        Long count = queryFactory
                .select(outbound.count())
                .from(outbound)
                .where(
                        outbound.warehouse.id.eq(warehouseId),
                        outbound.status.eq(status),
                        outbound.updatedAt.between(from, to),
                        activeEq()
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public List<OutboundListResult> findActiveWaitingQueue(Long warehouseId, Collection<OutboundStatus> statuses, int limit) {
        return queryFactory
                .select(Projections.constructor(
                        OutboundListResult.class,
                        outbound.id,
                        outbound.customerName,
                        outbound.status,
                        JPAExpressions.select(outboundItem.count())
                                .from(outboundItem)
                                .where(outboundItem.outbound.id.eq(outbound.id), outboundItem.active.isTrue()),
                        outbound.createdAt
                ))
                .from(outbound)
                .where(outbound.warehouse.id.eq(warehouseId), outbound.status.in(statuses), activeEq())
                .orderBy(outbound.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Map<OutboundStatus, Long> countActiveByCompanyAndCreatedAtBetweenGroupByStatus(
            Long companyId, LocalDateTime from, LocalDateTime to) {
        List<Tuple> rows = queryFactory
                .select(outbound.status, outbound.count())
                .from(outbound)
                .where(outbound.company.id.eq(companyId), outbound.createdAt.between(from, to), activeEq())
                .groupBy(outbound.status)
                .fetch();

        Map<OutboundStatus, Long> result = new EnumMap<>(OutboundStatus.class);
        for (Tuple row : rows) {
            result.put(row.get(outbound.status), row.get(outbound.count()));
        }
        return result;
    }

    @Override
    public Map<Long, Long> countActiveByWarehousesAndStatus(Collection<Long> warehouseIds, OutboundStatus status) {
        if (warehouseIds.isEmpty()) {
            return Map.of();
        }
        List<Tuple> rows = queryFactory
                .select(outbound.warehouse.id, outbound.count())
                .from(outbound)
                .where(outbound.warehouse.id.in(warehouseIds), outbound.status.eq(status), activeEq())
                .groupBy(outbound.warehouse.id)
                .fetch();

        Map<Long, Long> result = new HashMap<>();
        for (Tuple row : rows) {
            result.put(row.get(outbound.warehouse.id), row.get(outbound.count()));
        }
        return result;
    }

    @Override
    public List<LocalDateTime> findActiveUpdatedAtByCompanyAndStatusAndUpdatedAtBetween(
            Long companyId, OutboundStatus status, LocalDateTime from, LocalDateTime to) {
        return queryFactory
                .select(outbound.updatedAt)
                .from(outbound)
                .where(
                        outbound.company.id.eq(companyId),
                        outbound.status.eq(status),
                        outbound.updatedAt.between(from, to),
                        activeEq()
                )
                .fetch();
    }

    private BooleanExpression activeEq() {
        return outbound.active.isTrue();
    }

}
