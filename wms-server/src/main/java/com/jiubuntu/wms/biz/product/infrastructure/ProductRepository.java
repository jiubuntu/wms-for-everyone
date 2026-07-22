package com.jiubuntu.wms.biz.product.infrastructure;

import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.product.infrastructure.custom.CustomProductRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>, CustomProductRepository {
}
