package com.jiubuntu.wms.biz.warehouse.infrastructure.custom;

import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CustomWarehouseRepository {

    Optional<Warehouse> findActiveById(Long id);

    Page<Warehouse> findActiveByCompany(Long companyId, String keyword, Pageable pageable);

    Page<Warehouse> findActiveByCompanyAndId(Long companyId, Long id, String keyword, Pageable pageable);

    List<Warehouse> findAllActiveByCompany(Long companyId);

    List<Warehouse> findAllActiveByCompanyAndId(Long companyId, Long id);

}
