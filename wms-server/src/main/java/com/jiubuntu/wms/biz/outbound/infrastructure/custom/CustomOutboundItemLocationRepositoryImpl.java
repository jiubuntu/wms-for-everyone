package com.jiubuntu.wms.biz.outbound.infrastructure.custom;

import com.jiubuntu.wms.biz.location.domain.QLocation;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundAllocationRow;
import com.jiubuntu.wms.biz.outbound.domain.QOutboundItemLocation;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomOutboundItemLocationRepositoryImpl implements CustomOutboundItemLocationRepository {

    private static final QOutboundItemLocation outboundItemLocation = QOutboundItemLocation.outboundItemLocation;
    private static final QLocation location = QLocation.location;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<OutboundAllocationRow> findActiveRowsByOutboundItemIdIn(List<Long> outboundItemIds) {
        if (outboundItemIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
                .select(Projections.constructor(
                        OutboundAllocationRow.class,
                        outboundItemLocation.outboundItem.id,
                        location.code,
                        outboundItemLocation.lotNumber,
                        outboundItemLocation.quantity
                ))
                .from(outboundItemLocation)
                .join(outboundItemLocation.location, location)
                .where(outboundItemLocation.outboundItem.id.in(outboundItemIds), activeEq())
                .fetch();
    }

    private BooleanExpression activeEq() {
        return outboundItemLocation.active.isTrue();
    }

}
