package com.jiubuntu.wms.biz.inventory.infrastructure;

import com.jiubuntu.wms.biz.inventory.domain.InventoryHistory;
import com.jiubuntu.wms.biz.inventory.infrastructure.custom.CustomInventoryHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long>, CustomInventoryHistoryRepository {
}
