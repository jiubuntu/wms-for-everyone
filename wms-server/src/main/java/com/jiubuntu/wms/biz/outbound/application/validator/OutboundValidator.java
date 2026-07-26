package com.jiubuntu.wms.biz.outbound.application.validator;

import com.jiubuntu.wms.biz.outbound.domain.Outbound;
import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class OutboundValidator {

    public void validateUnit(Product product, ProductUnit unit) {
        boolean isBaseUnit = product.getBaseUnit().getId().equals(unit.getId());
        boolean isSubUnit = product.getSubUnit() != null && product.getSubUnit().getId().equals(unit.getId());
        if (!isBaseUnit && !isSubUnit) {
            throw new CommonException(ErrorCode.OUTBOUND_INVALID_UNIT);
        }
    }

    public void validateManualAllocationSum(int requestedQuantity, int allocatedSum) {
        if (requestedQuantity != allocatedSum) {
            throw new CommonException(ErrorCode.OUTBOUND_ALLOCATION_MISMATCH);
        }
    }

    public void validateComplete(Outbound outbound) {
        validateNotTerminal(outbound);
    }

    public void validateCancel(Outbound outbound) {
        validateNotTerminal(outbound);
    }

    private void validateNotTerminal(Outbound outbound) {
        if (outbound.getStatus() == OutboundStatus.COMPLETED) {
            throw new CommonException(ErrorCode.OUTBOUND_ALREADY_COMPLETED);
        }
        if (outbound.getStatus() == OutboundStatus.CANCELLED) {
            throw new CommonException(ErrorCode.OUTBOUND_ALREADY_CANCELLED);
        }
    }

}
