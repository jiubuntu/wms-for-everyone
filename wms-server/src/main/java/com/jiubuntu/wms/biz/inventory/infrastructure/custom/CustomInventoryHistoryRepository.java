package com.jiubuntu.wms.biz.inventory.infrastructure.custom;

import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryHistoryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomInventoryHistoryRepository {

    Page<InventoryHistoryResult> findByWarehouse(Long warehouseId, Pageable pageable);

}
