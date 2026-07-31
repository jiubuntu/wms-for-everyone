package com.jiubuntu.wms.biz.inventory.application;

import com.jiubuntu.wms.biz.inventory.application.dto.command.InventoryAdjustCommand;
import com.jiubuntu.wms.biz.inventory.application.dto.result.AvailableLocationResult;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryExpiringRow;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryHistoryResult;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryProductSummaryRow;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryResult;
import com.jiubuntu.wms.biz.inventory.application.validator.InventoryValidator;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.inventory.domain.InventoryHistory;
import com.jiubuntu.wms.biz.inventory.domain.InventoryHistoryTargetType;
import com.jiubuntu.wms.biz.inventory.infrastructure.InventoryHistoryRepository;
import com.jiubuntu.wms.biz.inventory.infrastructure.InventoryRepository;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.application.WarehouseService;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final InventoryValidator inventoryValidator;
    private final WarehouseService warehouseService;

    public Page<InventoryResult> list(Long warehouseId, Long companyId, UserRole role, Long principalWarehouseId,
                                       String keyword, Pageable pageable) {
        Long resolvedWarehouseId = resolveWarehouseId(warehouseId, role, principalWarehouseId);
        warehouseService.getAccessible(resolvedWarehouseId, companyId, role, principalWarehouseId);
        return inventoryRepository.findActiveByWarehouse(resolvedWarehouseId, keyword, pageable);
    }

    public List<AvailableLocationResult> getAvailableLocations(Long warehouseId, Long productId, Long companyId,
                                                                 UserRole role, Long principalWarehouseId) {
        warehouseService.getAccessible(warehouseId, companyId, role, principalWarehouseId);
        return inventoryRepository.findAvailableByWarehouseAndProduct(warehouseId, productId);
    }

    /**
     * 대시보드 집계용 — 호출측(DashboardService)에서 이미 창고 접근 권한을 확인했다고 가정하고,
     * 별도로 getAccessible()을 다시 거치지 않는다.
     */
    public List<InventoryExpiringRow> findExpiringSoon(Long warehouseId, int days, int limit) {
        LocalDate today = LocalDate.now();
        return inventoryRepository.findActiveExpiringSoon(warehouseId, today, today.plusDays(days), limit);
    }

    public long countExpiringSoon(Long warehouseId, int days) {
        LocalDate today = LocalDate.now();
        return inventoryRepository.countActiveExpiringSoon(warehouseId, today, today.plusDays(days));
    }

    public Map<Long, Long> countExpiringSoonGroupedByWarehouses(Collection<Long> warehouseIds, int days) {
        LocalDate today = LocalDate.now();
        return inventoryRepository.countActiveExpiringSoonGroupedByWarehouses(warehouseIds, today, today.plusDays(days));
    }

    public List<InventoryProductSummaryRow> findProductSummary(Long warehouseId) {
        return inventoryRepository.findActiveProductSummaryByWarehouse(warehouseId);
    }

    public Page<InventoryHistoryResult> getHistory(Long warehouseId, Long companyId, UserRole role,
                                                    Long principalWarehouseId, String keyword,
                                                    InventoryHistoryTargetType targetType, Pageable pageable) {
        Long resolvedWarehouseId = resolveWarehouseId(warehouseId, role, principalWarehouseId);
        warehouseService.getAccessible(resolvedWarehouseId, companyId, role, principalWarehouseId);
        return inventoryHistoryRepository.findByWarehouse(resolvedWarehouseId, keyword, targetType, pageable);
    }

    @Transactional
    public InventoryResult adjust(InventoryAdjustCommand command) {
        Inventory inventory = getActiveById(command.getId());
        Warehouse warehouse = inventory.getLocation().getWarehouse();
        warehouseService.getAccessible(warehouse.getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
        inventoryValidator.validateAdjust(inventory, command.getQuantity());

        int quantityChange = command.getQuantity() - inventory.getQuantity();
        inventory.adjustTo(command.getQuantity());
        inventory.assignUpdater(command.getUpdatedBy());

        recordHistory(inventory, warehouse, quantityChange, InventoryHistoryTargetType.ADJUSTMENT, null,
                command.getReason(), command.getUpdatedBy());

        return inventoryRepository.findResultById(inventory.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.INVENTORY_NOT_FOUND));
    }

    public Inventory getActiveByLocationProductLot(Long locationId, Long productId, String lotNumber) {
        return inventoryRepository.findActiveByLocationAndProductAndLotNumber(locationId, productId, lotNumber)
                .orElseThrow(() -> new CommonException(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY));
    }

    /**
     * 유효기간 임박 순(FEFO)으로 정렬된, 가용재고가 남은 재고 목록. 출고 FEFO 자동 할당에서 앞에서부터 소진한다.
     */
    public List<Inventory> findAvailableForAllocation(Long warehouseId, Long productId) {
        return inventoryRepository.findActiveAvailableForAllocation(warehouseId, productId);
    }

    @Transactional
    public Inventory reserve(Inventory inventory, int amount, Long updatedBy) {
        inventoryValidator.validateSufficientQuantity(inventory, amount);
        inventory.reserve(amount);
        inventory.assignUpdater(updatedBy);
        return inventoryRepository.saveAndFlush(inventory);
    }

    @Transactional
    public Inventory confirmReservation(Inventory inventory, int amount, Warehouse warehouse,
                                         InventoryHistoryTargetType targetType, Long targetId, Long updatedBy) {
        inventory.confirmReservation(amount);
        inventory.assignUpdater(updatedBy);
        Inventory saved = inventoryRepository.saveAndFlush(inventory);
        recordHistory(saved, warehouse, -amount, targetType, targetId, null, updatedBy);
        return saved;
    }

    @Transactional
    public Inventory releaseReservation(Inventory inventory, int amount, Long updatedBy) {
        inventory.releaseReservation(amount);
        inventory.assignUpdater(updatedBy);
        return inventoryRepository.saveAndFlush(inventory);
    }

    @Transactional
    public Inventory getOrCreateForLocation(Location location, Product product, String lotNumber,
                                             LocalDate manufactureDate, LocalDate expiryDate) {
        return inventoryRepository.findActiveByLocationAndProductAndLotNumber(location.getId(), product.getId(), lotNumber)
                .orElseGet(() -> inventoryRepository.save(
                        new Inventory(location, product, lotNumber, manufactureDate, expiryDate, 0)));
    }

    /**
     * 여러 위치를 한 트랜잭션에서 갱신할 때 location_id 오름차순으로 호출해 데드락을 회피한다.
     * 즉시 flush하여 실제 UPDATE/INSERT가 호출 순서대로 DB에 반영되도록 한다.
     */
    @Transactional
    public Inventory applyOrderedQuantityChange(Location location, Product product, String lotNumber,
                                                 LocalDate manufactureDate, LocalDate expiryDate, int delta) {
        Inventory target = getOrCreateForLocation(location, product, lotNumber, manufactureDate, expiryDate);
        if (delta < 0) {
            target.decreaseQuantity(-delta);
        } else {
            target.increaseQuantity(delta);
        }
        return inventoryRepository.saveAndFlush(target);
    }

    public void recordHistory(Inventory inventory, Warehouse warehouse, int quantityChange,
                               InventoryHistoryTargetType targetType, Long targetId, String reason, Long createdBy) {
        InventoryHistory history = new InventoryHistory(
                warehouse.getCompany(), warehouse, inventory.getLocation(), inventory.getProduct(),
                inventory.getLotNumber(), quantityChange, inventory.getQuantity(), targetType, targetId, reason, createdBy);
        inventoryHistoryRepository.save(history);
    }

    private Inventory getActiveById(Long id) {
        return inventoryRepository.findActiveById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.INVENTORY_NOT_FOUND));
    }

    private Long resolveWarehouseId(Long warehouseId, UserRole role, Long principalWarehouseId) {
        if (role == UserRole.COMPANY_ADMIN) {
            if (warehouseId == null) {
                throw new CommonException(ErrorCode.BAD_REQUEST);
            }
            return warehouseId;
        }
        return principalWarehouseId;
    }

}
