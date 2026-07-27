package com.jiubuntu.wms.biz.warehouse.infrastructure.custom;

import com.jiubuntu.wms.biz.warehouse.domain.QWarehouse;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
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

    @Override
    public Page<Warehouse> findActiveByCompany(Long companyId, Pageable pageable) {
        List<Warehouse> content = queryFactory
                .selectFrom(warehouse)
                .where(warehouse.company.id.eq(companyId), activeEq())
                .orderBy(warehouse.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(warehouse.count())
                .from(warehouse)
                .where(warehouse.company.id.eq(companyId), activeEq())
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Warehouse> findActiveByCompanyAndId(Long companyId, Long id, Pageable pageable) {
        if (id == null) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }

        List<Warehouse> content = queryFactory
                .selectFrom(warehouse)
                .where(warehouse.company.id.eq(companyId), warehouse.id.eq(id), activeEq())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(warehouse.count())
                .from(warehouse)
                .where(warehouse.company.id.eq(companyId), warehouse.id.eq(id), activeEq())
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Warehouse> findAllActiveByCompany(Long companyId) {
        return queryFactory.selectFrom(warehouse)
                .where(warehouse.company.id.eq(companyId), activeEq())
                .orderBy(warehouse.createdAt.asc())
                .fetch();
    }

    @Override
    public List<Warehouse> findAllActiveByCompanyAndId(Long companyId, Long id) {
        if (id == null) {
            return List.of();
        }
        return queryFactory.selectFrom(warehouse)
                .where(warehouse.company.id.eq(companyId), warehouse.id.eq(id), activeEq())
                .fetch();
    }

    private BooleanExpression activeEq() {
        return warehouse.active.isTrue();
    }

}
