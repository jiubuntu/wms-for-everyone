package com.jiubuntu.wms.biz.warehouse.infrastructure.custom;

import com.jiubuntu.wms.biz.warehouse.domain.QWarehouse;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CustomWarehouseRepositoryImpl implements CustomWarehouseRepository {

    private static final QWarehouse warehouse = QWarehouse.warehouse;

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Warehouse> findActiveById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(warehouse)
                        .where(warehouse.id.eq(id), activeEq())
                        .fetchOne()
        );
    }

    private BooleanExpression activeEq() {
        return warehouse.active.isTrue();
    }

}
