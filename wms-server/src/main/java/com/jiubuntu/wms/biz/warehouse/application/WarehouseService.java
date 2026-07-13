package com.jiubuntu.wms.biz.warehouse.application;

import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.biz.warehouse.infrastructure.WarehouseRepository;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public Warehouse getActiveById(Long id) {
        return warehouseRepository.findActiveById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.WAREHOUSE_NOT_FOUND));
    }

}
