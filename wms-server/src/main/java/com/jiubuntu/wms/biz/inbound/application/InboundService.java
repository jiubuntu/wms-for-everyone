package com.jiubuntu.wms.biz.inbound.application;

import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundActionCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundItemCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundLocationCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundLocationReplaceCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundRegisterCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundHeaderResult;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundItemResult;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundItemRow;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundListResult;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundLocationResult;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundLocationRow;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundResult;
import com.jiubuntu.wms.biz.inbound.application.validator.InboundValidator;
import com.jiubuntu.wms.biz.inbound.domain.Inbound;
import com.jiubuntu.wms.biz.inbound.domain.InboundItem;
import com.jiubuntu.wms.biz.inbound.domain.InboundItemLocation;
import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import com.jiubuntu.wms.biz.inbound.infrastructure.InboundItemLocationRepository;
import com.jiubuntu.wms.biz.inbound.infrastructure.InboundItemRepository;
import com.jiubuntu.wms.biz.inbound.infrastructure.InboundRepository;
import com.jiubuntu.wms.biz.inventory.application.InventoryService;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.inventory.domain.InventoryHistoryTargetType;
import com.jiubuntu.wms.biz.location.application.LocationService;
import com.jiubuntu.wms.biz.location.domain.Location;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InboundService {

    private static final int MAX_COMPLETE_ATTEMPTS = 3;

    private final InboundRepository inboundRepository;
    private final InboundItemRepository inboundItemRepository;
    private final InboundItemLocationRepository inboundItemLocationRepository;
    private final InboundValidator inboundValidator;
    private final InventoryService inventoryService;
    private final WarehouseService warehouseService;
    private final LocationService locationService;
    private final ProductService productService;
    private final ProductUnitService productUnitService;
    private final PlatformTransactionManager transactionManager;

    public Page<InboundListResult> list(Long warehouseId, Long companyId, UserRole role, Long principalWarehouseId,
                                         Pageable pageable) {
        warehouseService.getAccessible(warehouseId, companyId, role, principalWarehouseId);
        return inboundRepository.findActiveByWarehouse(warehouseId, pageable);
    }

    public InboundResult getDetail(Long id, Long companyId, UserRole role, Long principalWarehouseId) {
        InboundHeaderResult header = inboundRepository.findHeaderResultById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.INBOUND_NOT_FOUND));
        warehouseService.getAccessible(header.getWarehouseId(), companyId, role, principalWarehouseId);
        return assembleResult(header);
    }

    @Transactional
    public InboundResult register(InboundRegisterCommand command) {
        Warehouse warehouse = warehouseService.getAccessible(
                command.getWarehouseId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());

        Inbound inbound = new Inbound(warehouse.getCompany(), warehouse, command.getSupplierName(), command.getNote());
        inbound.assignCreator(command.getCreatedBy());
        inbound = inboundRepository.save(inbound);

        for (InboundItemCommand itemCommand : command.getItems()) {
            Product product = productService.getAccessible(itemCommand.getProductId(), command.getCompanyId());
            ProductUnit unit = productUnitService.getAccessible(itemCommand.getUnitId(), command.getCompanyId());
            inboundValidator.validateUnit(product, unit);
            inboundValidator.validateLotFields(product, itemCommand.getLotNumber(), itemCommand.getManufactureDate(),
                    itemCommand.getExpiryDate());

            InboundItem item = new InboundItem(inbound, product, unit, itemCommand.getQuantity(),
                    itemCommand.getLotNumber(), itemCommand.getManufactureDate(), itemCommand.getExpiryDate());
            item.assignCreator(command.getCreatedBy());
            inboundItemRepository.save(item);
        }

        return getDetail(inbound.getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
    }

    @Transactional
    public InboundResult replaceItemLocations(InboundLocationReplaceCommand command) {
        Inbound inbound = getActiveById(command.getInboundId());
        warehouseService.getAccessible(
                inbound.getWarehouse().getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
        inboundValidator.validateModifiable(inbound);

        InboundItem item = getActiveItem(command.getItemId(), inbound.getId());
        int locationsSum = command.getLocations().stream().mapToInt(InboundLocationCommand::getQuantity).sum();
        inboundValidator.validateLocationSum(item.getQuantity(), locationsSum);

        List<InboundItemLocation> existing = inboundItemLocationRepository.findByInboundItemIdAndActiveTrue(item.getId());
        for (InboundItemLocation old : existing) {
            old.delete();
            old.assignUpdater(command.getActorId());
        }

        for (InboundLocationCommand locationCommand : command.getLocations()) {
            Location location = locationService.getActiveInWarehouse(locationCommand.getLocationId(), command.getWarehouseId());
            InboundItemLocation newLocation = new InboundItemLocation(item, location, locationCommand.getQuantity());
            newLocation.assignCreator(command.getActorId());
            inboundItemLocationRepository.save(newLocation);
        }

        if (inbound.getStatus() == InboundStatus.PENDING) {
            inbound.startProcessing();
            inbound.assignUpdater(command.getActorId());
        }

        return getDetail(inbound.getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
    }

    /**
     * 낙관적 락 충돌 시 시도마다 완전히 새 트랜잭션으로 재조회부터 다시 수행한다 (최대 3회).
     * 같은 빈 안에서 @Transactional 메서드를 자기 호출하면 프록시를 안 타 트랜잭션이 안 걸리므로,
     * TransactionTemplate(REQUIRES_NEW)으로 시도마다 명시적으로 새 트랜잭션을 연다.
     */
    public InboundResult complete(InboundActionCommand command) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        for (int attempt = 1; attempt <= MAX_COMPLETE_ATTEMPTS; attempt++) {
            try {
                return transactionTemplate.execute(status -> completeOnce(command));
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == MAX_COMPLETE_ATTEMPTS) {
                    throw new CommonException(ErrorCode.INBOUND_CONCURRENT_UPDATE_CONFLICT);
                }
            }
        }
        throw new IllegalStateException("재시도 루프를 벗어날 수 없습니다.");
    }

    private InboundResult completeOnce(InboundActionCommand command) {
        Inbound inbound = getActiveById(command.getInboundId());
        Warehouse warehouse = warehouseService.getAccessible(
                inbound.getWarehouse().getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
        inboundValidator.validateComplete(inbound);

        List<InboundItem> items = inboundItemRepository.findByInboundIdAndActiveTrue(inbound.getId());
        List<InboundPlacementApplication> applications = new ArrayList<>();
        for (InboundItem item : items) {
            List<InboundItemLocation> locations = inboundItemLocationRepository.findByInboundItemIdAndActiveTrue(item.getId());
            int placedSum = locations.stream().mapToInt(InboundItemLocation::getQuantity).sum();
            inboundValidator.validateFullyPlaced(item.getQuantity(), placedSum);

            for (InboundItemLocation location : locations) {
                int baseQuantity = toBaseQuantity(item.getProduct(), item.getUnit(), location.getQuantity());
                applications.add(new InboundPlacementApplication(location.getLocation(), item.getProduct(),
                        item.getLotNumber(), item.getManufactureDate(), item.getExpiryDate(), baseQuantity));
            }
        }

        // location_id 오름차순으로 재고 반영 — 여러 재고 row를 한 트랜잭션에서 갱신할 때의 데드락 회피
        applications.sort(Comparator.comparing(application -> application.getLocation().getId()));
        for (InboundPlacementApplication application : applications) {
            Inventory updated = inventoryService.applyOrderedQuantityChange(application.getLocation(), application.getProduct(),
                    application.getLotNumber(), application.getManufactureDate(), application.getExpiryDate(),
                    application.getBaseQuantity());
            inventoryService.recordHistory(updated, warehouse, application.getBaseQuantity(),
                    InventoryHistoryTargetType.INBOUND, inbound.getId(), null, command.getActorId());
        }

        inbound.complete(command.getActorId());
        inbound.assignUpdater(command.getActorId());

        return getDetail(inbound.getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
    }

    @Transactional
    public InboundResult cancel(InboundActionCommand command) {
        Inbound inbound = getActiveById(command.getInboundId());
        warehouseService.getAccessible(
                inbound.getWarehouse().getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
        inboundValidator.validateCancel(inbound);

        inbound.cancel();
        inbound.assignUpdater(command.getActorId());

        return getDetail(inbound.getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
    }

    private int toBaseQuantity(Product product, ProductUnit unit, int quantity) {
        if (product.getSubUnit() != null && product.getSubUnit().getId().equals(unit.getId())) {
            return BigDecimal.valueOf(quantity)
                    .multiply(product.getUnitConversionRate())
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValueExact();
        }
        return quantity;
    }

    private InboundResult assembleResult(InboundHeaderResult header) {
        List<InboundItemRow> itemRows = inboundItemRepository.findActiveRowsByInboundId(header.getId());
        List<Long> itemIds = itemRows.stream().map(InboundItemRow::getId).toList();
        List<InboundLocationRow> locationRows = inboundItemLocationRepository.findActiveRowsByInboundItemIdIn(itemIds);

        Map<Long, List<InboundLocationResult>> locationsByItemId = locationRows.stream()
                .collect(Collectors.groupingBy(InboundLocationRow::getInboundItemId, Collectors.mapping(
                        row -> new InboundLocationResult(row.getLocationId(), row.getLocationCode(), row.getQuantity()),
                        Collectors.toList())));

        List<InboundItemResult> items = itemRows.stream()
                .map(row -> new InboundItemResult(row.getId(), row.getProductId(), row.getProductSkuCode(), row.getProductName(),
                        row.getUnitId(), row.getUnitName(), row.getQuantity(), row.getLotNumber(), row.getManufactureDate(),
                        row.getExpiryDate(), locationsByItemId.getOrDefault(row.getId(), List.of())))
                .toList();

        return new InboundResult(header.getId(), header.getWarehouseId(), header.getSupplierName(), header.getStatus(),
                header.getNote(), header.getProcessedByName(), header.getCreatedAt(), items);
    }

    private Inbound getActiveById(Long id) {
        return inboundRepository.findActiveById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.INBOUND_NOT_FOUND));
    }

    private InboundItem getActiveItem(Long itemId, Long inboundId) {
        InboundItem item = inboundItemRepository.findById(itemId)
                .filter(InboundItem::isActive)
                .orElseThrow(() -> new CommonException(ErrorCode.INBOUND_NOT_FOUND));
        if (!item.getInbound().getId().equals(inboundId)) {
            throw new CommonException(ErrorCode.INBOUND_NOT_FOUND);
        }
        return item;
    }

}
