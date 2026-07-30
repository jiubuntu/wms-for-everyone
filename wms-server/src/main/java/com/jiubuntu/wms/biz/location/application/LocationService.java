package com.jiubuntu.wms.biz.location.application;

import com.jiubuntu.wms.biz.commoncode.application.CommonCodeService;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.location.application.dto.command.LocationBulkCreateCommand;
import com.jiubuntu.wms.biz.location.application.dto.command.LocationDeleteCommand;
import com.jiubuntu.wms.biz.location.application.dto.command.LocationUpdateCommand;
import com.jiubuntu.wms.biz.location.application.dto.result.LocationResult;
import com.jiubuntu.wms.biz.location.application.validator.LocationValidator;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.location.infrastructure.LocationRepository;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.application.WarehouseService;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationValidator locationValidator;
    private final WarehouseService warehouseService;
    private final CommonCodeService commonCodeService;

    public Page<LocationResult> list(Long warehouseId, Long companyId, UserRole role, Long principalWarehouseId,
                                      String keyword, Pageable pageable) {
        warehouseService.getAccessible(warehouseId, companyId, role, principalWarehouseId);
        return locationRepository.findActiveByWarehouse(warehouseId, keyword, pageable).map(this::toResult);
    }

    public List<LocationResult> listAll(Long warehouseId, Long companyId, UserRole role, Long principalWarehouseId) {
        warehouseService.getAccessible(warehouseId, companyId, role, principalWarehouseId);
        return locationRepository.findAllActiveByWarehouse(warehouseId).stream()
                .map(this::toResult)
                .toList();
    }

    public Map<Long, Long> countActiveByWarehouseIds(Collection<Long> warehouseIds) {
        return locationRepository.countActiveByWarehouseIds(warehouseIds);
    }

    @Transactional
    public List<Location> bulkCreate(LocationBulkCreateCommand command) {
        Warehouse warehouse = warehouseService.getAccessible(
                command.getWarehouseId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());
        locationValidator.validateRange(
                command.getRowFrom(), command.getRowTo(), command.getColFrom(), command.getColTo(), command.getLevelCount());

        CommonCode storageType = command.getStorageTypeId() != null
                ? commonCodeService.getAccessible(command.getStorageTypeId(), CommonCodeGroup.STORAGE_TYPE, command.getCompanyId())
                : null;

        List<Location> toCreate = buildLocations(warehouse, command, storageType);
        locationValidator.validateNoDuplicateCodes(warehouse.getId(), toCreate.stream().map(Location::getCode).toList());
        toCreate.forEach(location -> location.assignCreator(command.getCreatedBy()));

        return locationRepository.saveAll(toCreate);
    }

    @Transactional
    public Location update(LocationUpdateCommand command) {
        Location location = getActiveInWarehouse(command.getId(), command.getWarehouseId());
        warehouseService.getAccessible(
                location.getWarehouse().getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());

        CommonCode storageType = command.getStorageTypeId() != null
                ? commonCodeService.getAccessible(command.getStorageTypeId(), CommonCodeGroup.STORAGE_TYPE, command.getCompanyId())
                : null;

        location.update(storageType, command.getStatus());
        location.assignUpdater(command.getUpdatedBy());
        return location;
    }

    @Transactional
    public void delete(LocationDeleteCommand command) {
        Location location = getActiveInWarehouse(command.getId(), command.getWarehouseId());
        warehouseService.getAccessible(
                location.getWarehouse().getId(), command.getCompanyId(), command.getRole(), command.getPrincipalWarehouseId());

        location.assignUpdater(command.getUpdatedBy());
        location.delete();
    }

    private List<Location> buildLocations(Warehouse warehouse, LocationBulkCreateCommand command, CommonCode storageType) {
        List<Location> locations = new ArrayList<>();
        for (int row = command.getRowFrom(); row <= command.getRowTo(); row++) {
            for (int col = command.getColFrom(); col <= command.getColTo(); col++) {
                for (int level = 1; level <= command.getLevelCount(); level++) {
                    String rowStr = String.format("%02d", row);
                    String colStr = String.format("%02d", col);
                    String levelStr = String.valueOf(level);
                    String code = command.getZone() + "-" + rowStr + "-" + colStr + "-" + levelStr;

                    locations.add(Location.builder()
                            .warehouse(warehouse)
                            .zone(command.getZone())
                            .row(rowStr)
                            .col(colStr)
                            .level(levelStr)
                            .code(code)
                            .storageType(storageType)
                            .build());
                }
            }
        }
        return locations;
    }

    public Location getActiveInWarehouse(Long id, Long warehouseId) {
        Location location = locationRepository.findActiveById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.LOCATION_NOT_FOUND));
        if (!location.getWarehouse().getId().equals(warehouseId)) {
            throw new CommonException(ErrorCode.LOCATION_NOT_FOUND);
        }
        return location;
    }

    private LocationResult toResult(Location location) {
        return LocationResult.builder()
                .id(location.getId())
                .warehouseId(location.getWarehouse() != null ? location.getWarehouse().getId() : null)
                .zone(location.getZone())
                .row(location.getRow())
                .col(location.getCol())
                .level(location.getLevel())
                .code(location.getCode())
                .storageTypeId(location.getStorageType() != null ? location.getStorageType().getId() : null)
                .status(location.getStatus())
                .createdAt(location.getCreatedAt())
                .build();
    }

}
