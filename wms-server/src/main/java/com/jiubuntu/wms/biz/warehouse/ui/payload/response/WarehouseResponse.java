package com.jiubuntu.wms.biz.warehouse.ui.payload.response;

import com.jiubuntu.wms.biz.warehouse.application.dto.result.WarehouseResult;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WarehouseResponse {

    private final Long id;
    private final Long companyId;
    private final String name;
    private final String address;
    private final Long locationCount;
    private final boolean active;
    private final LocalDateTime createdAt;

    private WarehouseResponse(Long id, Long companyId, String name, String address,
                               Long locationCount, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.address = address;
        this.locationCount = locationCount;
        this.active = active;
        this.createdAt = createdAt;
    }

    public static WarehouseResponse from(WarehouseResult result, Long locationCount) {
        return new WarehouseResponse(
                result.getId(),
                result.getCompanyId(),
                result.getName(),
                result.getAddress(),
                locationCount,
                result.isActive(),
                result.getCreatedAt()
        );
    }

    public static WarehouseResponse from(WarehouseResult result) {
        return from(result, null);
    }

    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCompany() != null ? warehouse.getCompany().getId() : null,
                warehouse.getName(),
                warehouse.getAddress(),
                null,
                warehouse.isActive(),
                warehouse.getCreatedAt()
        );
    }

}
