package com.jiubuntu.wms.biz.product.infrastructure.custom;

import com.jiubuntu.wms.biz.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CustomProductRepository {

    boolean existsActiveByCompanyAndSkuCode(Long companyId, String skuCode);

    Optional<Product> findActiveById(Long id);

    Page<Product> findActiveByCompany(Long companyId, String keyword, Pageable pageable);

    List<Product> findAllActiveByCompany(Long companyId);

    long countActiveByCompany(Long companyId);

}
