package com.jiubuntu.wms.biz.inbound.application.dto.command;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

/**
 * 입고 확정(complete)·취소(cancel) 공통 입력 — 두 유스케이스가 필요로 하는 정보가 동일해 하나로 묶는다.
 */
@Getter
@Builder
public class InboundActionCommand {

    private final Long inboundId;
    private final Long warehouseId;
    private final Long companyId;
    private final UserRole role;
    private final Long principalWarehouseId;
    private final Long actorId;

}
