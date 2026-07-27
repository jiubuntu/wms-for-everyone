package com.jiubuntu.wms.biz.inbound.application.dto.command;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InboundRegisterCommand {

    private final Long warehouseId;
    private final Long companyId;
    private final UserRole role;
    private final Long principalWarehouseId;
    private final String supplierName;
    private final String note;
    private final List<InboundItemCommand> items;
    private final Long createdBy;

}
