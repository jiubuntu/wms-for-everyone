package com.jiubuntu.wms.biz.location.application.dto.command;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import lombok.Getter;

@Getter
public class LocationDeleteCommand {

    private final Long id;
    private final Long warehouseId;
    private final Long companyId;
    private final UserRole role;
    private final Long principalWarehouseId;
    private final Long updatedBy;

    public LocationDeleteCommand(Long id, Long warehouseId, Long companyId, UserRole role, Long principalWarehouseId, Long updatedBy) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.companyId = companyId;
        this.role = role;
        this.principalWarehouseId = principalWarehouseId;
        this.updatedBy = updatedBy;
    }

}
