package com.jiubuntu.wms.biz.outbound.application;

import com.jiubuntu.wms.biz.inventory.application.InventoryService;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.inventory.domain.InventoryHistoryTargetType;
import com.jiubuntu.wms.biz.outbound.application.dto.command.OutboundActionCommand;
import com.jiubuntu.wms.biz.outbound.application.dto.command.OutboundItemCommand;
import com.jiubuntu.wms.biz.outbound.application.dto.command.OutboundRegisterCommand;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundAllocationResult;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundAllocationRow;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundHeaderResult;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundItemResult;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundItemRow;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundListResult;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundResult;
import com.jiubuntu.wms.biz.outbound.application.validator.OutboundValidator;
import com.jiubuntu.wms.biz.outbound.domain.Outbound;
import com.jiubuntu.wms.biz.outbound.domain.OutboundItem;
import com.jiubuntu.wms.biz.outbound.domain.OutboundItemLocation;
import com.jiubuntu.wms.biz.outbound.infrastructure.OutboundItemLocationRepository;
import com.jiubuntu.wms.biz.outbound.infrastructure.OutboundItemRepository;
import com.jiubuntu.wms.biz.outbound.infrastructure.OutboundRepository;
import com.jiubuntu.wms.biz.product.application.ProductService;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.productunit.application.ProductUnitService;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.application.WarehouseService;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboundService {

    private static final int MAX_ATTEMPTS = 3;

    private final OutboundRepository outboundRepository;
    private final OutboundItemRepository outboundItemRepository;
    private final OutboundItemLocationRepository outboundItemLocationRepository;
    private final OutboundValidator outboundValidator;
    private final OutboundAllocationPlanner outboundAllocationPlanner;
    private final InventoryService inventoryService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final ProductUnitService productUnitService;
    private final PlatformTransactionManager transactionManager;

    public Page<OutboundListResult> list(Long warehouseId, Long companyId, UserRole role, Long principalWarehouseId,
                                          Pageable pageable) {
        warehouseService.getAccessible(warehouseId, companyId, role, principalWarehouseId);
        return outboundRepository.findActiveByWarehouse(warehouseId, pageable);
    }

    public OutboundResult getDetail(Long id, Long companyId, UserRole role, Long principalWarehouseId) {
        OutboundHeaderResult header = outboundRepository.findHeaderResultById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.OUTBOUND_NOT_FOUND));
        warehouseService.getAccessible(header.getWarehouseId(), companyId, role, principalWarehouseId);
        return assembleResult(header);
    }

    /**
     * 낙관적 락 충돌 시 시도마다 완전히 새 트랜잭션으로 재조회부터 다시 수행한다 (최대 3회).
     * 같은 빈 안에서 @Transactional 메서드를 자기 호출하면 프록시를 안 타 트랜잭션이 안 걸리므로,
     * TransactionTemplate(REQUIRES_NEW)으로 시도마다 명시적으로 새 트랜잭션을 연다.
     */
    public OutboundResult register(OutboundRegisterCommand command) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return transactionTemplate.execute(status -> registerOnce(command));
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new CommonException(ErrorCode.OUTBOUND_CONCURRENT_UPDATE_CONFLICT);
                }
            }
        }
        throw new IllegalStateException("재시도 루프를 벗어날 수 없습니다.");
    }

    private OutboundResult registerOnce(OutboundRegisterCommand command) {
        Warehouse warehouse = warehouseService.getAccessible(
                command.getWarehouseId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());

        Outbound outbound = new Outbound(warehouse.getCompany(), warehouse, command.getCustomerName(), command.getNote());
        outbound.assignCreator(command.getCreatedBy());
        outbound = outboundRepository.save(outbound);

        List<OutboundPendingReservation> pendingReservations = new ArrayList<>();
        for (OutboundItemCommand itemCommand : command.getItems()) {
            Product product = productService.getAccessible(itemCommand.getProductId(), command.getCompanyId());
            ProductUnit unit = productUnitService.getAccessible(itemCommand.getUnitId(), command.getCompanyId());
            outboundValidator.validateUnit(product, unit);

            OutboundItem item = new OutboundItem(outbound, product, unit, itemCommand.getQuantity(), itemCommand.getAllocationType());
            item.assignCreator(command.getCreatedBy());
            item = outboundItemRepository.save(item);

            List<OutboundAllocationPlan> plans = outboundAllocationPlanner.plan(command.getWarehouseId(), product, unit,
                    itemCommand.getQuantity(), itemCommand.getAllocationType(), itemCommand.getAllocations());

            for (OutboundAllocationPlan plan : plans) {
                pendingReservations.add(new OutboundPendingReservation(
                        item, plan.getLocation(), plan.getLotNumber(), plan.getInventory(), plan.getQuantity()));
            }
        }

        applyReservationsInOrder(pendingReservations, command.getCreatedBy());

        return getDetail(outbound.getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
    }

    /**
     * register()와 동일한 낙관적 락 재시도 패턴.
     */
    public OutboundResult complete(OutboundActionCommand command) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return transactionTemplate.execute(status -> completeOnce(command));
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new CommonException(ErrorCode.OUTBOUND_CONCURRENT_UPDATE_CONFLICT);
                }
            }
        }
        throw new IllegalStateException("재시도 루프를 벗어날 수 없습니다.");
    }

    private OutboundResult completeOnce(OutboundActionCommand command) {
        Outbound outbound = getActiveById(command.getOutboundId());
        Warehouse warehouse = warehouseService.getAccessible(
                outbound.getWarehouse().getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
        outboundValidator.validateComplete(outbound);

        List<OutboundReservedAllocation> reserved = loadReservedAllocations(outbound.getId());
        reserved.sort(Comparator.comparing(allocation -> allocation.getLocation().getId()));
        for (OutboundReservedAllocation allocation : reserved) {
            Inventory inventory = inventoryService.getActiveByLocationProductLot(
                    allocation.getLocation().getId(), allocation.getProductId(), allocation.getLotNumber());
            inventoryService.confirmReservation(inventory, allocation.getQuantity(), warehouse,
                    InventoryHistoryTargetType.OUTBOUND, outbound.getId(), command.getActorId());
        }

        outbound.complete(command.getActorId());
        outbound.assignUpdater(command.getActorId());

        return getDetail(outbound.getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
    }

    /**
     * register()와 동일한 낙관적 락 재시도 패턴.
     */
    public OutboundResult cancel(OutboundActionCommand command) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return transactionTemplate.execute(status -> cancelOnce(command));
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new CommonException(ErrorCode.OUTBOUND_CONCURRENT_UPDATE_CONFLICT);
                }
            }
        }
        throw new IllegalStateException("재시도 루프를 벗어날 수 없습니다.");
    }

    private OutboundResult cancelOnce(OutboundActionCommand command) {
        Outbound outbound = getActiveById(command.getOutboundId());
        warehouseService.getAccessible(
                outbound.getWarehouse().getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
        outboundValidator.validateCancel(outbound);

        List<OutboundReservedAllocation> reserved = loadReservedAllocations(outbound.getId());
        reserved.sort(Comparator.comparing(allocation -> allocation.getLocation().getId()));
        for (OutboundReservedAllocation allocation : reserved) {
            Inventory inventory = inventoryService.getActiveByLocationProductLot(
                    allocation.getLocation().getId(), allocation.getProductId(), allocation.getLotNumber());
            inventoryService.releaseReservation(inventory, allocation.getQuantity(), command.getActorId());
        }

        outbound.cancel();
        outbound.assignUpdater(command.getActorId());

        return getDetail(outbound.getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
    }

    /**
     * location_id 오름차순으로 예약을 반영한다 — 여러 재고 row를 한 트랜잭션에서 갱신할 때의 데드락 회피 원칙
     */
    private void applyReservationsInOrder(List<OutboundPendingReservation> pendingReservations, Long actorId) {
        pendingReservations.sort(Comparator.comparing(reservation -> reservation.getLocation().getId()));
        for (OutboundPendingReservation reservation : pendingReservations) {
            inventoryService.reserve(reservation.getInventory(), reservation.getQuantity(), actorId);
            OutboundItemLocation itemLocation = new OutboundItemLocation(
                    reservation.getItem(), reservation.getLocation(), reservation.getLotNumber(), reservation.getQuantity());
            itemLocation.assignCreator(actorId);
            outboundItemLocationRepository.save(itemLocation);
        }
    }

    private List<OutboundReservedAllocation> loadReservedAllocations(Long outboundId) {
        List<OutboundItem> items = outboundItemRepository.findByOutboundIdAndActiveTrue(outboundId);
        Map<Long, Long> productIdByItemId = items.stream()
                .collect(Collectors.toMap(OutboundItem::getId, item -> item.getProduct().getId()));
        List<Long> itemIds = items.stream().map(OutboundItem::getId).toList();

        List<OutboundItemLocation> itemLocations = outboundItemLocationRepository.findByOutboundItemIdInAndActiveTrue(itemIds);
        List<OutboundReservedAllocation> reserved = new ArrayList<>();
        for (OutboundItemLocation itemLocation : itemLocations) {
            Long productId = productIdByItemId.get(itemLocation.getOutboundItem().getId());
            reserved.add(new OutboundReservedAllocation(
                    itemLocation.getLocation(), itemLocation.getLotNumber(), productId, itemLocation.getQuantity()));
        }
        return reserved;
    }

    private OutboundResult assembleResult(OutboundHeaderResult header) {
        List<OutboundItemRow> itemRows = outboundItemRepository.findActiveRowsByOutboundId(header.getId());
        List<Long> itemIds = itemRows.stream().map(OutboundItemRow::getId).toList();
        List<OutboundAllocationRow> allocationRows = outboundItemLocationRepository.findActiveRowsByOutboundItemIdIn(itemIds);

        Map<Long, List<OutboundAllocationResult>> allocationsByItemId = allocationRows.stream()
                .collect(Collectors.groupingBy(OutboundAllocationRow::getOutboundItemId, Collectors.mapping(
                        row -> new OutboundAllocationResult(row.getLocationCode(), row.getLotNumber(), row.getQuantity()),
                        Collectors.toList())));

        List<OutboundItemResult> items = itemRows.stream()
                .map(row -> new OutboundItemResult(row.getId(), row.getProductId(), row.getProductSkuCode(), row.getProductName(),
                        row.getUnitId(), row.getUnitName(), row.getQuantity(), row.getAllocationType(),
                        allocationsByItemId.getOrDefault(row.getId(), List.of())))
                .toList();

        return new OutboundResult(header.getId(), header.getWarehouseId(), header.getCustomerName(), header.getStatus(),
                header.getNote(), header.getProcessedByName(), header.getCreatedAt(), items);
    }

    private Outbound getActiveById(Long id) {
        return outboundRepository.findActiveById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.OUTBOUND_NOT_FOUND));
    }

}
