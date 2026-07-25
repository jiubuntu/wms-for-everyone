package com.jiubuntu.wms.biz.inventory.infrastructure.custom;

import com.jiubuntu.wms.biz.commoncode.domain.QCommonCode;
import com.jiubuntu.wms.biz.inventory.application.dto.result.TransferResult;
import com.jiubuntu.wms.biz.inventory.domain.QTransfer;
import com.jiubuntu.wms.biz.location.domain.QLocation;
import com.jiubuntu.wms.biz.product.domain.QProduct;
import com.jiubuntu.wms.biz.productunit.domain.QProductUnit;
import com.jiubuntu.wms.biz.user.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.JPQLQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomTransferRepositoryImpl implements CustomTransferRepository {

    private static final QTransfer transfer = QTransfer.transfer;
    private static final QProduct product = QProduct.product;
    private static final QProductUnit baseUnit = new QProductUnit("transferBaseUnit");
    private static final QLocation fromLocation = new QLocation("transferFromLocation");
    private static final QLocation toLocation = new QLocation("transferToLocation");
    private static final QUser createdByUser = new QUser("transferCreatedBy");
    private static final QCommonCode reason = new QCommonCode("transferReason");

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<TransferResult> findResultById(Long id) {
        return Optional.ofNullable(baseResultQuery().where(transfer.id.eq(id), activeEq()).fetchOne());
    }

    @Override
    public Page<TransferResult> findActiveByWarehouse(Long warehouseId, Pageable pageable) {
        List<TransferResult> content = baseResultQuery()
                .where(transfer.warehouse.id.eq(warehouseId), activeEq())
                .orderBy(transfer.createdAt.desc(), transfer.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(transfer.count())
                .from(transfer)
                .where(transfer.warehouse.id.eq(warehouseId), activeEq())
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private JPQLQuery<TransferResult> baseResultQuery() {
        return queryFactory
                .select(Projections.constructor(
                        TransferResult.class,
                        transfer.id,
                        transfer.warehouse.id,
                        product.id,
                        product.skuCode,
                        product.name,
                        baseUnit.name,
                        fromLocation.id,
                        fromLocation.code,
                        toLocation.id,
                        toLocation.code,
                        transfer.lotNumber,
                        transfer.quantity,
                        reason.id,
                        transfer.note,
                        createdByUser.name,
                        transfer.createdAt
                ))
                .from(transfer)
                .join(transfer.product, product)
                .join(product.baseUnit, baseUnit)
                .join(transfer.fromLocation, fromLocation)
                .join(transfer.toLocation, toLocation)
                .join(createdByUser).on(createdByUser.id.eq(transfer.createdBy))
                .leftJoin(transfer.reason, reason);
    }

    private BooleanExpression activeEq() {
        return transfer.active.isTrue();
    }

}
