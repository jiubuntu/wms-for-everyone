package com.jiubuntu.wms.biz.inbound.infrastructure.custom;

import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundLocationRow;
import com.jiubuntu.wms.biz.inbound.domain.QInboundItemLocation;
import com.jiubuntu.wms.biz.location.domain.QLocation;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomInboundItemLocationRepositoryImpl implements CustomInboundItemLocationRepository {

    private static final QInboundItemLocation inboundItemLocation = QInboundItemLocation.inboundItemLocation;
    private static final QLocation location = QLocation.location;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<InboundLocationRow> findActiveRowsByInboundItemIdIn(List<Long> inboundItemIds) {
        if (inboundItemIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
                .select(Projections.constructor(
                        InboundLocationRow.class,
                        inboundItemLocation.inboundItem.id,
                        location.id,
                        location.code,
                        inboundItemLocation.quantity
                ))
                .from(inboundItemLocation)
                .join(inboundItemLocation.location, location)
                .where(inboundItemLocation.inboundItem.id.in(inboundItemIds), activeEq())
                .fetch();
    }

    private BooleanExpression activeEq() {
        return inboundItemLocation.active.isTrue();
    }

}
