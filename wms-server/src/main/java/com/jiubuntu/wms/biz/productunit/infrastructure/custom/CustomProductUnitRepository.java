package com.jiubuntu.wms.biz.productunit.infrastructure.custom;

import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomProductUnitRepository {

    boolean existsActiveByCompanyAndName(Long companyId, String name);

    Optional<ProductUnit> findActiveById(Long id);

    Page<ProductUnit> findActiveByCompany(Long companyId, Pageable pageable);

}
