package com.jiubuntu.wms.biz.warehouse.infrastructure;

import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.biz.warehouse.infrastructure.custom.CustomWarehouseRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long>, CustomWarehouseRepository {
}
