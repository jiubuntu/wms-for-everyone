package com.jiubuntu.wms.biz.warehouse.application.dto.command;

import lombok.Getter;

@Getter
public class WarehouseDeleteCommand {

    private final Long id;
    private final Long expectedCompanyId;
    private final Long updatedBy;

    public WarehouseDeleteCommand(Long id, Long expectedCompanyId, Long updatedBy) {
        this.id = id;
        this.expectedCompanyId = expectedCompanyId;
        this.updatedBy = updatedBy;
    }

}
