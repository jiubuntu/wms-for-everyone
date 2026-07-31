package com.jiubuntu.wms.biz.location.application.validator;

import com.jiubuntu.wms.biz.location.infrastructure.LocationRepository;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationValidator {

    private final LocationRepository locationRepository;

    public void validateRange(int rowFrom, int rowTo, int colFrom, int colTo, int levelCount) {
        if (rowFrom < 1 || colFrom < 1 || levelCount < 1 || rowFrom > rowTo || colFrom > colTo) {
            throw new CommonException(ErrorCode.LOCATION_INVALID_RANGE);
        }
    }

    public void validateNoDuplicateCodes(Long warehouseId, List<String> codes) {
        if (!locationRepository.findActiveCodesByWarehouseAndCodeIn(warehouseId, codes).isEmpty()) {
            throw new CommonException(ErrorCode.LOCATION_ALREADY_EXISTS);
        }
    }

}
