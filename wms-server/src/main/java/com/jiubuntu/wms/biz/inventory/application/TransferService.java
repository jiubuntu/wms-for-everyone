package com.jiubuntu.wms.biz.inventory.application;

import com.jiubuntu.wms.biz.commoncode.application.CommonCodeService;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.inventory.application.dto.command.TransferCreateCommand;
import com.jiubuntu.wms.biz.inventory.application.dto.result.TransferResult;
import com.jiubuntu.wms.biz.inventory.application.validator.InventoryValidator;
import com.jiubuntu.wms.biz.inventory.application.validator.TransferValidator;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.inventory.domain.InventoryHistoryTargetType;
import com.jiubuntu.wms.biz.inventory.domain.Transfer;
import com.jiubuntu.wms.biz.inventory.infrastructure.TransferRepository;
import com.jiubuntu.wms.biz.location.application.LocationService;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.product.application.ProductService;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.application.WarehouseService;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferService {

    private final TransferRepository transferRepository;
    private final TransferValidator transferValidator;
    private final InventoryValidator inventoryValidator;
    private final InventoryService inventoryService;
    private final WarehouseService warehouseService;
    private final LocationService locationService;
    private final ProductService productService;
    private final CommonCodeService commonCodeService;

    public Page<TransferResult> list(Long warehouseId, Long companyId, UserRole role, Long principalWarehouseId,
                                      Pageable pageable) {
        warehouseService.getAccessible(warehouseId, companyId, role, principalWarehouseId);
        return transferRepository.findActiveByWarehouse(warehouseId, pageable);
    }

    @Transactional
    public TransferResult create(TransferCreateCommand command) {
        Warehouse warehouse = warehouseService.getAccessible(
                command.getWarehouseId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
        Location fromLocation = locationService.getActiveInWarehouse(command.getFromLocationId(), command.getWarehouseId());
        Location toLocation = locationService.getActiveInWarehouse(command.getToLocationId(), command.getWarehouseId());
        transferValidator.validateLocations(fromLocation, toLocation);

        Product product = productService.getAccessible(command.getProductId(), command.getCompanyId());
        CommonCode reason = resolveReason(command);

        Inventory sourceInventory = inventoryService.getActiveByLocationProductLot(
                fromLocation.getId(), product.getId(), command.getLotNumber());
        inventoryValidator.validateSufficientQuantity(sourceInventory, command.getQuantity());

        OrderedInventories updated = applyOrderedChanges(
                fromLocation, toLocation, product, command.getLotNumber(), sourceInventory, command.getQuantity());

        Transfer saved = saveTransfer(warehouse, product, fromLocation, toLocation, reason, command);
        recordTransferHistory(updated, warehouse, reason, saved, command);

        return transferRepository.findResultById(saved.getId())
                .orElseThrow(() -> new IllegalStateException("방금 저장한 이동 건을 조회하지 못했습니다."));
    }

    private CommonCode resolveReason(TransferCreateCommand command) {
        return command.getReasonId() != null
                ? commonCodeService.getAccessible(command.getReasonId(), CommonCodeGroup.TRANSFER_REASON, command.getCompanyId())
                : null;
    }

    /**
     * location_id 오름차순으로 먼저/나중 위치를 정해 순서대로 갱신한다
     */
    private OrderedInventories applyOrderedChanges(Location fromLocation, Location toLocation, Product product,
                                                    String lotNumber, Inventory sourceInventory, int quantity) {
        boolean fromFirst = fromLocation.getId() < toLocation.getId();
        Location firstLocation = fromFirst ? fromLocation : toLocation;
        Location secondLocation = fromFirst ? toLocation : fromLocation;
        int firstDelta = fromFirst ? -quantity : quantity;
        int secondDelta = fromFirst ? quantity : -quantity;

        Inventory firstInventory = inventoryService.applyOrderedQuantityChange(firstLocation, product, lotNumber,
                sourceInventory.getManufactureDate(), sourceInventory.getExpiryDate(), firstDelta);
        Inventory secondInventory = inventoryService.applyOrderedQuantityChange(secondLocation, product, lotNumber,
                sourceInventory.getManufactureDate(), sourceInventory.getExpiryDate(), secondDelta);

        Inventory fromInventory = fromFirst ? firstInventory : secondInventory;
        Inventory toInventory = fromFirst ? secondInventory : firstInventory;
        return new OrderedInventories(fromInventory, toInventory);
    }

    private Transfer saveTransfer(Warehouse warehouse, Product product, Location fromLocation, Location toLocation,
                                   CommonCode reason, TransferCreateCommand command) {
        Transfer transfer = new Transfer(warehouse.getCompany(), warehouse, product, fromLocation, toLocation,
                command.getLotNumber(), command.getQuantity(), reason, command.getNote());
        transfer.assignCreator(command.getCreatedBy());
        return transferRepository.save(transfer);
    }

    private void recordTransferHistory(OrderedInventories updated, Warehouse warehouse, CommonCode reason,
                                        Transfer saved, TransferCreateCommand command) {
        String reasonName = reason != null ? reason.getName() : null;
        inventoryService.recordHistory(updated.from, warehouse, -command.getQuantity(),
                InventoryHistoryTargetType.TRANSFER, saved.getId(), reasonName, command.getCreatedBy());
        inventoryService.recordHistory(updated.to, warehouse, command.getQuantity(),
                InventoryHistoryTargetType.TRANSFER, saved.getId(), reasonName, command.getCreatedBy());
    }

    private static class OrderedInventories {

        private final Inventory from;
        private final Inventory to;

        private OrderedInventories(Inventory from, Inventory to) {
            this.from = from;
            this.to = to;
        }

    }

}
