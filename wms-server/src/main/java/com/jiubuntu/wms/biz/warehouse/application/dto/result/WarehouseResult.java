package com.jiubuntu.wms.biz.warehouse.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WarehouseResult {

    private final Long id;
    private final Long companyId;
    private final String name;
    private final String address;
    private final boolean active;
    private final LocalDateTime createdAt;

}
