package com.jiubuntu.wms.biz.inbound.application.validator;

import com.jiubuntu.wms.biz.inbound.domain.Inbound;
import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class InboundValidator {

    public void validateUnit(Product product, ProductUnit unit) {
        boolean isBaseUnit = product.getBaseUnit().getId().equals(unit.getId());
        boolean isSubUnit = product.getSubUnit() != null && product.getSubUnit().getId().equals(unit.getId());
        if (!isBaseUnit && !isSubUnit) {
            throw new CommonException(ErrorCode.INBOUND_INVALID_UNIT);
        }
    }

    public void validateLotFields(Product product, String lotNumber, LocalDate manufactureDate, LocalDate expiryDate) {
        if (product.isLotTracking() && (lotNumber == null || manufactureDate == null || expiryDate == null)) {
            throw new CommonException(ErrorCode.INBOUND_LOT_INFO_REQUIRED);
        }
    }

    public void validateLocationSum(int requestedQuantity, int locationsSum) {
        if (requestedQuantity != locationsSum) {
            throw new CommonException(ErrorCode.INBOUND_LOCATION_SUM_MISMATCH);
        }
    }

    public void validateFullyPlaced(int requestedQuantity, int placedSum) {
        if (requestedQuantity != placedSum) {
            throw new CommonException(ErrorCode.INBOUND_NOT_FULLY_PLACED);
        }
    }

    public void validateModifiable(Inbound inbound) {
        validateNotTerminal(inbound);
    }

    public void validateComplete(Inbound inbound) {
        validateNotTerminal(inbound);
    }

    public void validateCancel(Inbound inbound) {
        validateNotTerminal(inbound);
    }

    private void validateNotTerminal(Inbound inbound) {
        if (inbound.getStatus() == InboundStatus.COMPLETED) {
            throw new CommonException(ErrorCode.INBOUND_ALREADY_COMPLETED);
        }
        if (inbound.getStatus() == InboundStatus.CANCELLED) {
            throw new CommonException(ErrorCode.INBOUND_ALREADY_CANCELLED);
        }
    }

}
