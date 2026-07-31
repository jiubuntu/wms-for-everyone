package com.jiubuntu.wms.biz.productunit.infrastructure;

import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.biz.productunit.infrastructure.custom.CustomProductUnitRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long>, CustomProductUnitRepository {
}
