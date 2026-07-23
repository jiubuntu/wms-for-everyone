package com.jiubuntu.wms.biz.location.infrastructure.custom;

import com.jiubuntu.wms.biz.location.domain.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CustomLocationRepository {

    Optional<Location> findActiveById(Long id);

    Page<Location> findActiveByWarehouse(Long warehouseId, Pageable pageable);

    List<String> findActiveCodesByWarehouseAndCodeIn(Long warehouseId, Collection<String> codes);

    Map<Long, Long> countActiveByWarehouseIds(Collection<Long> warehouseIds);

}
