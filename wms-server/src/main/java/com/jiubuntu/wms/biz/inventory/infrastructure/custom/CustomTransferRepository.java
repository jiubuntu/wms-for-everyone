package com.jiubuntu.wms.biz.inventory.infrastructure.custom;

import com.jiubuntu.wms.biz.inventory.application.dto.result.TransferResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomTransferRepository {

    Optional<TransferResult> findResultById(Long id);

    Page<TransferResult> findActiveByWarehouse(Long warehouseId, Pageable pageable);

}
