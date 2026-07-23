package com.jiubuntu.wms.biz.location.ui.payload.response;

import com.jiubuntu.wms.biz.location.application.dto.result.LocationResult;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.location.domain.LocationStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LocationResponse {

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

    private LocationResponse(Long id, Long warehouseId, String zone, String row, String col, String level,
                              String code, Long storageTypeId, LocationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.zone = zone;
        this.row = row;
        this.col = col;
        this.level = level;
        this.code = code;
        this.storageTypeId = storageTypeId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static LocationResponse from(LocationResult result) {
        return new LocationResponse(
                result.getId(),
                result.getWarehouseId(),
                result.getZone(),
                result.getRow(),
                result.getCol(),
                result.getLevel(),
                result.getCode(),
                result.getStorageTypeId(),
                result.getStatus(),
                result.getCreatedAt()
        );
    }

    public static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getWarehouse() != null ? location.getWarehouse().getId() : null,
                location.getZone(),
                location.getRow(),
                location.getCol(),
                location.getLevel(),
                location.getCode(),
                location.getStorageType() != null ? location.getStorageType().getId() : null,
                location.getStatus(),
                location.getCreatedAt()
        );
    }

}
