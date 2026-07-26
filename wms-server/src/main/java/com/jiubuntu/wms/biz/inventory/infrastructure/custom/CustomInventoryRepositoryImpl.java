package com.jiubuntu.wms.biz.inventory.infrastructure.custom;

import com.jiubuntu.wms.biz.inventory.application.dto.result.AvailableLocationResult;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryResult;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.inventory.domain.QInventory;
import com.jiubuntu.wms.biz.location.domain.QLocation;
import com.jiubuntu.wms.biz.product.domain.QProduct;
import com.jiubuntu.wms.biz.productunit.domain.QProductUnit;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomInventoryRepositoryImpl implements CustomInventoryRepository {

    private static final QInventory inventory = QInventory.inventory;
    private static final QLocation location = QLocation.location;
    private static final QProduct product = QProduct.product;
    private static final QProductUnit baseUnit = new QProductUnit("inventoryBaseUnit");

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Inventory> findActiveById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(inventory)
                        .where(inventory.id.eq(id), activeEq())
                        .fetchOne()
        );
    }

    @Override
    public Optional<Inventory> findActiveByLocationAndProductAndLotNumber(Long locationId, Long productId, String lotNumber) {
        return Optional.ofNullable(
                queryFactory.selectFrom(inventory)
                        .where(inventory.location.id.eq(locationId),
                                inventory.product.id.eq(productId),
                                lotNumberEq(lotNumber),
                                activeEq())
                        .fetchOne()
        );
    }

    @Override
    public Optional<InventoryResult> findResultById(Long id) {
        InventoryResult result = queryFactory
                .select(Projections.constructor(
                        InventoryResult.class,
                        inventory.id,
                        location.warehouse.id,
                        location.id,
                        location.code,
                        product.id,
                        product.skuCode,
                        product.name,
                        baseUnit.name,
                        inventory.lotNumber,
                        inventory.manufactureDate,
                        inventory.expiryDate,
                        inventory.quantity,
                        inventory.reservedQuantity
                ))
                .from(inventory)
                .join(inventory.location, location)
                .join(inventory.product, product)
                .join(product.baseUnit, baseUnit)
                .where(inventory.id.eq(id), activeEq())
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Page<InventoryResult> findActiveByWarehouse(Long warehouseId, Pageable pageable) {
        List<InventoryResult> content = queryFactory
                .select(Projections.constructor(
                        InventoryResult.class,
                        inventory.id,
                        location.warehouse.id,
                        location.id,
                        location.code,
                        product.id,
                        product.skuCode,
                        product.name,
                        baseUnit.name,
                        inventory.lotNumber,
                        inventory.manufactureDate,
                        inventory.expiryDate,
                        inventory.quantity,
                        inventory.reservedQuantity
                ))
                .from(inventory)
                .join(inventory.location, location)
                .join(inventory.product, product)
                .join(product.baseUnit, baseUnit)
                .where(location.warehouse.id.eq(warehouseId), activeEq())
                .orderBy(location.code.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(inventory.count())
                .from(inventory)
                .join(inventory.location, location)
                .where(location.warehouse.id.eq(warehouseId), activeEq())
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<AvailableLocationResult> findAvailableByWarehouseAndProduct(Long warehouseId, Long productId) {
        return queryFactory
                .select(Projections.constructor(
                        AvailableLocationResult.class,
                        location.id,
                        location.code,
                        inventory.lotNumber,
                        inventory.expiryDate,
                        inventory.quantity.subtract(inventory.reservedQuantity)
                ))
                .from(inventory)
                .join(inventory.location, location)
                .where(location.warehouse.id.eq(warehouseId),
                        inventory.product.id.eq(productId),
                        inventory.quantity.subtract(inventory.reservedQuantity).gt(0),
                        activeEq())
                .orderBy(inventory.expiryDate.asc().nullsLast())
                .fetch();
    }

    @Override
    public List<Inventory> findActiveAvailableForAllocation(Long warehouseId, Long productId) {
        return queryFactory.selectFrom(inventory)
                .join(inventory.location, location).fetchJoin()
                .where(location.warehouse.id.eq(warehouseId),
                        inventory.product.id.eq(productId),
                        inventory.quantity.subtract(inventory.reservedQuantity).gt(0),
                        activeEq())
                .orderBy(inventory.expiryDate.asc().nullsLast())
                .fetch();
    }

    private BooleanExpression lotNumberEq(String lotNumber) {
        return lotNumber != null ? inventory.lotNumber.eq(lotNumber) : inventory.lotNumber.isNull();
    }

    private BooleanExpression activeEq() {
        return inventory.active.isTrue();
    }

}
