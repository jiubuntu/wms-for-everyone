package com.jiubuntu.wms.biz.inbound.infrastructure.custom;

import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundHeaderResult;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundListResult;
import com.jiubuntu.wms.biz.inbound.domain.Inbound;
import com.jiubuntu.wms.biz.inbound.domain.QInbound;
import com.jiubuntu.wms.biz.inbound.domain.QInboundItem;
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
public class CustomInboundRepositoryImpl implements CustomInboundRepository {

    private static final QInbound inbound = QInbound.inbound;
    private static final QInboundItem inboundItem = QInboundItem.inboundItem;
    private static final QUser processedByUser = new QUser("inboundProcessedByUser");

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Inbound> findActiveById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(inbound)
                        .where(inbound.id.eq(id), activeEq())
                        .fetchOne()
        );
    }

    @Override
    public Optional<InboundHeaderResult> findHeaderResultById(Long id) {
        InboundHeaderResult result = queryFactory
                .select(Projections.constructor(
                        InboundHeaderResult.class,
                        inbound.id,
                        inbound.warehouse.id,
                        inbound.supplierName,
                        inbound.status,
                        inbound.note,
                        processedByUser.name,
                        inbound.createdAt
                ))
                .from(inbound)
                .leftJoin(processedByUser).on(processedByUser.id.eq(inbound.processedBy))
                .where(inbound.id.eq(id), activeEq())
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Page<InboundListResult> findActiveByWarehouse(Long warehouseId, Pageable pageable) {
        List<InboundListResult> content = queryFactory
                .select(Projections.constructor(
                        InboundListResult.class,
                        inbound.id,
                        inbound.supplierName,
                        inbound.status,
                        JPAExpressions.select(inboundItem.count())
                                .from(inboundItem)
                                .where(inboundItem.inbound.id.eq(inbound.id), inboundItem.active.isTrue()),
                        inbound.createdAt
                ))
                .from(inbound)
                .where(inbound.warehouse.id.eq(warehouseId), activeEq())
                .orderBy(inbound.createdAt.desc(), inbound.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(inbound.count())
                .from(inbound)
                .where(inbound.warehouse.id.eq(warehouseId), activeEq())
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression activeEq() {
        return inbound.active.isTrue();
    }

}
