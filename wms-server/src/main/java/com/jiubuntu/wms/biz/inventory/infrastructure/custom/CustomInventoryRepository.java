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

    List<Inventory> findActiveByLocationIdInAndProductIdIn(Collection<Long> locationIds, Collection<Long> productIds);

    Optional<InventoryResult> findResultById(Long id);

    Page<InventoryResult> findActiveByWarehouse(Long warehouseId, String keyword, Pageable pageable);

    List<AvailableLocationResult> findAvailableByWarehouseAndProduct(Long warehouseId, Long productId);

    List<Inventory> findActiveAvailableForAllocation(Long warehouseId, Long productId);

    /**
     * item 개수(정확히는 distinct 상품 개수)만큼 findActiveAvailableForAllocation()을 반복 호출하는 대신,
     * 요청에 포함된 상품 id 전체의 후보를 한 번에 조회한다. 유효기간 오름차순 정렬은 그대로 유지되어
     * 호출측에서 상품별로 묶어도 각 그룹 내 순서(FEFO)가 보존된다.
     */
    List<Inventory> findActiveAvailableForAllocationIn(Long warehouseId, Collection<Long> productIds);

    List<InventoryExpiringRow> findActiveExpiringSoon(Long warehouseId, LocalDate from, LocalDate to, int limit);

    long countActiveExpiringSoon(Long warehouseId, LocalDate from, LocalDate to);

    Map<Long, Long> countActiveExpiringSoonGroupedByWarehouses(Collection<Long> warehouseIds, LocalDate from, LocalDate to);

    List<InventoryProductSummaryRow> findActiveProductSummaryByWarehouse(Long warehouseId);

}
