package com.jiubuntu.wms.biz.outbound.infrastructure.custom;

import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundItemRow;
import com.jiubuntu.wms.biz.outbound.domain.QOutboundItem;
import com.jiubuntu.wms.biz.product.domain.QProduct;
import com.jiubuntu.wms.biz.productunit.domain.QProductUnit;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomOutboundItemRepositoryImpl implements CustomOutboundItemRepository {

    private static final QOutboundItem outboundItem = QOutboundItem.outboundItem;
    private static final QProduct product = QProduct.product;
    private static final QProductUnit unit = QProductUnit.productUnit;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<OutboundItemRow> findActiveRowsByOutboundId(Long outboundId) {
        return queryFactory
                .select(Projections.constructor(
                        OutboundItemRow.class,
                        outboundItem.id,
                        product.id,
                        product.skuCode,
                        product.name,
                        unit.id,
                        unit.name,
                        outboundItem.quantity,
                        outboundItem.allocationType
                ))
                .from(outboundItem)
                .join(outboundItem.product, product)
                .join(outboundItem.unit, unit)
                .where(outboundItem.outbound.id.eq(outboundId), activeEq())
                .orderBy(outboundItem.id.asc())
                .fetch();
    }

    private BooleanExpression activeEq() {
        return outboundItem.active.isTrue();
    }

}
