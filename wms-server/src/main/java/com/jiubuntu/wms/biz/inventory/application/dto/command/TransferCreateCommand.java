package com.jiubuntu.wms.biz.inventory.application.dto.command;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransferCreateCommand {

    private final Long warehouseId;
    private final Long companyId;
    private final UserRole role;
    private final Long principalWarehouseId;
    private final Long productId;
    private final Long fromLocationId;
    private final Long toLocationId;
    private final String lotNumber;
    private final int quantity;
    private final Long reasonId;
    private final String note;
    private final Long createdBy;

}
