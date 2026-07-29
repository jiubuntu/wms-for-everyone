package com.jiubuntu.wms.biz.warehouse.application.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WarehouseCreateCommand {

    private final Long companyId;
    private final String name;
    private final String address;

}
