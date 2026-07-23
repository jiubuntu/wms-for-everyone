package com.jiubuntu.wms.biz.location.infrastructure.custom;

import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.location.domain.QLocation;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomLocationRepositoryImpl implements CustomLocationRepository {

    private static final QLocation location = QLocation.location;

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Location> findActiveById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(location)
                        .where(location.id.eq(id), activeEq())
                        .fetchOne()
        );
    }

    @Override
    public Page<Location> findActiveByWarehouse(Long warehouseId, Pageable pageable) {
        List<Location> content = queryFactory
                .selectFrom(location)
                .where(location.warehouse.id.eq(warehouseId), activeEq())
                .orderBy(location.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(location.count())
                .from(location)
                .where(location.warehouse.id.eq(warehouseId), activeEq())
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<String> findActiveCodesByWarehouseAndCodeIn(Long warehouseId, Collection<String> codes) {
        if (codes.isEmpty()) {
            return List.of();
        }
        return queryFactory
                .select(location.code)
                .from(location)
                .where(location.warehouse.id.eq(warehouseId), location.code.in(codes), activeEq())
                .fetch();
    }

    @Override
    public Map<Long, Long> countActiveByWarehouseIds(Collection<Long> warehouseIds) {
        if (warehouseIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = queryFactory
                .select(location.warehouse.id, location.count())
                .from(location)
                .where(location.warehouse.id.in(warehouseIds), activeEq())
                .groupBy(location.warehouse.id)
                .fetch();

        Map<Long, Long> result = new HashMap<>();
        for (Tuple row : rows) {
            result.put(row.get(location.warehouse.id), row.get(location.count()));
        }
        return result;
    }

    private BooleanExpression activeEq() {
        return location.active.isTrue();
    }

}
