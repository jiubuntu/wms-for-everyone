package com.jiubuntu.wms.biz.warehouse.application.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WarehouseUpdateCommand {

    private final Long id;
    private final Long expectedCompanyId;
    private final String name;
    private final Long storageTypeId;
    private final String address;
    private final Long updatedBy;

}
