package com.jiubuntu.wms.biz.warehouse.infrastructure.custom;

import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;

import java.util.Optional;

public interface CustomWarehouseRepository {

    Optional<Warehouse> findActiveById(Long id);

}
