package com.jiubuntu.wms.biz.location.application.dto.command;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationBulkCreateCommand {

    private final Long warehouseId;
    private final Long companyId;
    private final UserRole role;
    private final Long principalWarehouseId;
    private final String zone;
    private final int rowFrom;
    private final int rowTo;
    private final int colFrom;
    private final int colTo;
    private final int levelCount;
    private final Long storageTypeId;
    private final Long createdBy;

}
