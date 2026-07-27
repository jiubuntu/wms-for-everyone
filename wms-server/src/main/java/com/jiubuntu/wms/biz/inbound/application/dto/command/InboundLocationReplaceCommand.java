package com.jiubuntu.wms.biz.inbound.application.dto.command;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InboundLocationReplaceCommand {

    private final Long inboundId;
    private final Long itemId;
    private final Long warehouseId;
    private final Long companyId;
    private final UserRole role;
    private final Long principalWarehouseId;
    private final List<InboundLocationCommand> locations;
    private final Long actorId;

}
