package com.jiubuntu.wms.biz.warehouse.infrastructure.custom;

import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomWarehouseRepository {

    Optional<Warehouse> findActiveById(Long id);

    Page<Warehouse> findActiveByCompany(Long companyId, Pageable pageable);

    Page<Warehouse> findActiveByCompanyAndId(Long companyId, Long id, Pageable pageable);

}
