package com.jiubuntu.wms.biz.location.application.dto.command;

import com.jiubuntu.wms.biz.location.domain.LocationStatus;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationUpdateCommand {

    private final Long id;
    private final Long warehouseId;
    private final Long companyId;
    private final UserRole role;
    private final Long principalWarehouseId;
    private final Long storageTypeId;
    private final LocationStatus status;
    private final Long updatedBy;

}
