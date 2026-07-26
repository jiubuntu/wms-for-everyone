package com.jiubuntu.wms.biz.outbound.infrastructure.custom;

import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundHeaderResult;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundListResult;
import com.jiubuntu.wms.biz.outbound.domain.Outbound;
import com.jiubuntu.wms.biz.outbound.domain.QOutbound;
import com.jiubuntu.wms.biz.outbound.domain.QOutboundItem;
import com.jiubuntu.wms.biz.user.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
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

    private BooleanExpression activeEq() {
        return outbound.active.isTrue();
    }

}
