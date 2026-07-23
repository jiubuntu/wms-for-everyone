package com.jiubuntu.wms.biz.inventory.application.dto.command;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import lombok.Getter;

@Getter
public class InventoryAdjustCommand {

    private final Long id;
    private final Long companyId;
    private final UserRole role;
    private final Long principalWarehouseId;
    private final int quantity;
    private final String reason;
    private final Long updatedBy;

    public InventoryAdjustCommand(Long id, Long companyId, UserRole role, Long principalWarehouseId, int quantity,
                                   String reason, Long updatedBy) {
        this.id = id;
        this.companyId = companyId;
        this.role = role;
        this.principalWarehouseId = principalWarehouseId;
        this.quantity = quantity;
        this.reason = reason;
        this.updatedBy = updatedBy;
    }

}
