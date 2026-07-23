package com.jiubuntu.wms.biz.inventory.application.validator;

import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class InventoryValidator {

    public void validateAdjust(Inventory inventory, int newQuantity) {
        if (newQuantity < inventory.getReservedQuantity()) {
            throw new CommonException(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY);
        }
    }

    public void validateSufficientQuantity(Inventory inventory, int requiredQuantity) {
        if (inventory.getAvailableQuantity() < requiredQuantity) {
            throw new CommonException(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY);
        }
    }

}
