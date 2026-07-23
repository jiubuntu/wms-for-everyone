package com.jiubuntu.wms.biz.inventory.application.validator;

import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.location.domain.LocationStatus;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class TransferValidator {

    public void validateLocations(Location fromLocation, Location toLocation) {
        if (fromLocation.getId().equals(toLocation.getId())) {
            throw new CommonException(ErrorCode.TRANSFER_SAME_LOCATION);
        }
        if (toLocation.getStatus() == LocationStatus.DISABLED) {
            throw new CommonException(ErrorCode.TRANSFER_LOCATION_DISABLED);
        }
    }

}
