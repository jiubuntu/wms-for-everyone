package com.jiubuntu.wms.biz.inbound.infrastructure.custom;

import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundItemRow;
import com.jiubuntu.wms.biz.inbound.domain.QInboundItem;
import com.jiubuntu.wms.biz.product.domain.QProduct;
import com.jiubuntu.wms.biz.productunit.domain.QProductUnit;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomInboundItemRepositoryImpl implements CustomInboundItemRepository {

    private static final QInboundItem inboundItem = QInboundItem.inboundItem;
    private static final QProduct product = QProduct.product;
    private static final QProductUnit unit = QProductUnit.productUnit;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<InboundItemRow> findActiveRowsByInboundId(Long inboundId) {
        return queryFactory
                .select(Projections.constructor(
                        InboundItemRow.class,
                        inboundItem.id,
                        product.id,
                        product.skuCode,
                        product.name,
                        unit.id,
                        unit.name,
                        inboundItem.quantity,
                        inboundItem.lotNumber,
                        inboundItem.manufactureDate,
                        inboundItem.expiryDate
                ))
                .from(inboundItem)
                .join(inboundItem.product, product)
                .join(inboundItem.unit, unit)
                .where(inboundItem.inbound.id.eq(inboundId), activeEq())
                .orderBy(inboundItem.id.asc())
                .fetch();
    }

    private BooleanExpression activeEq() {
        return inboundItem.active.isTrue();
    }

}
