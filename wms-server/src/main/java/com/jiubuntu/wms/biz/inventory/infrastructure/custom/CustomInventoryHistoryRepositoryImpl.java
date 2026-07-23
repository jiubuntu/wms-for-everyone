package com.jiubuntu.wms.biz.inventory.infrastructure.custom;

import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryHistoryResult;
import com.jiubuntu.wms.biz.inventory.domain.QInventoryHistory;
import com.jiubuntu.wms.biz.location.domain.QLocation;
import com.jiubuntu.wms.biz.product.domain.QProduct;
import com.jiubuntu.wms.biz.user.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class CustomInventoryHistoryRepositoryImpl implements CustomInventoryHistoryRepository {

    private static final QInventoryHistory history = QInventoryHistory.inventoryHistory;
    private static final QLocation location = QLocation.location;
    private static final QProduct product = QProduct.product;
    private static final QUser createdByUser = new QUser("historyCreatedBy");

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<InventoryHistoryResult> findByWarehouse(Long warehouseId, Pageable pageable) {
        List<InventoryHistoryResult> content = queryFactory
                .select(Projections.constructor(
                        InventoryHistoryResult.class,
                        history.id,
                        history.warehouse.id,
                        location.code,
                        product.skuCode,
                        product.name,
                        history.lotNumber,
                        history.quantityChange,
                        history.quantityAfter,
                        history.targetType,
                        history.targetId,
                        history.reason,
                        history.createdAt,
                        createdByUser.name
                ))
                .from(history)
                .join(history.location, location)
                .join(history.product, product)
                .join(createdByUser).on(createdByUser.id.eq(history.createdBy))
                .where(history.warehouse.id.eq(warehouseId))
                .orderBy(history.createdAt.desc(), history.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(history.count())
                .from(history)
                .where(history.warehouse.id.eq(warehouseId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

}
