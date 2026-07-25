package com.jiubuntu.wms.biz.inventory.infrastructure;

import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.inventory.infrastructure.custom.CustomInventoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, CustomInventoryRepository {
}
