package com.jiubuntu.wms.biz.productunit.infrastructure.custom;

import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CustomProductUnitRepository {

    boolean existsActiveByCompanyAndName(Long companyId, String name);

    Optional<ProductUnit> findActiveById(Long id);

    List<ProductUnit> findAllActiveByIdIn(Collection<Long> ids);

    Page<ProductUnit> findActiveByCompany(Long companyId, String keyword, Pageable pageable);

    List<ProductUnit> findActiveByCompany(Long companyId);

}
