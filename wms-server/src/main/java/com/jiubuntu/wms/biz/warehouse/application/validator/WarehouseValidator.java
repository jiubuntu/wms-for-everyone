package com.jiubuntu.wms.biz.warehouse.application.validator;

import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.biz.warehouse.domain.WarehouseStaffAssignmentPort;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarehouseValidator {

    private final WarehouseStaffAssignmentPort warehouseStaffAssignmentPort;

    public void validateDelete(Warehouse warehouse) {
        if (warehouseStaffAssignmentPort.hasActiveStaff(warehouse.getId())) {
            throw new CommonException(ErrorCode.WAREHOUSE_HAS_ASSIGNED_STAFF);
        }
    }

}
