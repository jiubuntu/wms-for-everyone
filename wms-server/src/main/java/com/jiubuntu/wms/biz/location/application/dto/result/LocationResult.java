package com.jiubuntu.wms.biz.location.application.dto.result;

import com.jiubuntu.wms.biz.location.domain.LocationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LocationResult {

    private final Long id;
    private final Long warehouseId;
    private final String zone;
    private final String row;
    private final String col;
    private final String level;
    private final String code;
    private final Long storageTypeId;
    private final LocationStatus status;
    private final LocalDateTime createdAt;

}
