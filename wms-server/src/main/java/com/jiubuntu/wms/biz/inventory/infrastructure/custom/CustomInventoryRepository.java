package com.jiubuntu.wms.biz.inventory.infrastructure.custom;

import com.jiubuntu.wms.biz.inventory.application.dto.result.AvailableLocationResult;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryExpiringRow;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryProductSummaryRow;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryResult;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CustomInventoryRepository {

    Optional<Inventory> findActiveById(Long id);

    Optional<Inventory> findActiveByLocationAndProductAndLotNumber(Long locationId, Long productId, String lotNumber);

    Optional<InventoryResult> findResultById(Long id);

    Page<InventoryResult> findActiveByWarehouse(Long warehouseId, Pageable pageable);

    List<AvailableLocationResult> findAvailableByWarehouseAndProduct(Long warehouseId, Long productId);

    List<Inventory> findActiveAvailableForAllocation(Long warehouseId, Long productId);

    List<InventoryExpiringRow> findActiveExpiringSoon(Long warehouseId, LocalDate from, LocalDate to, int limit);

    long countActiveExpiringSoon(Long warehouseId, LocalDate from, LocalDate to);

    Map<Long, Long> countActiveExpiringSoonGroupedByWarehouses(Collection<Long> warehouseIds, LocalDate from, LocalDate to);

    List<InventoryProductSummaryRow> findActiveProductSummaryByWarehouse(Long warehouseId);

}
