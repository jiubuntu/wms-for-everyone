package com.jiubuntu.wms.biz.product.application.validator;

import com.jiubuntu.wms.biz.product.application.dto.command.ProductCreateCommand;
import com.jiubuntu.wms.biz.product.application.dto.command.ProductUpdateCommand;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.product.infrastructure.ProductRepository;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProductValidator {

    private final ProductRepository productRepository;

    public void validateCreate(ProductCreateCommand command) {
        if (productRepository.existsActiveByCompanyAndSkuCode(command.getCompanyId(), command.getSkuCode())) {
            throw new CommonException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
        validateSubUnitConversion(command.getSubUnitId(), command.getUnitConversionRate());
    }

    public void validateUpdate(ProductUpdateCommand command) {
        validateSubUnitConversion(command.getSubUnitId(), command.getUnitConversionRate());
    }

    public void validateScope(Product existing, Long expectedCompanyId) {
        Long existingCompanyId = existing.getCompany() != null ? existing.getCompany().getId() : null;
        if (!Objects.equals(existingCompanyId, expectedCompanyId)) {
            throw new CommonException(ErrorCode.COMPANY_SCOPE_VIOLATION);
        }
    }

    private void validateSubUnitConversion(Long subUnitId, BigDecimal unitConversionRate) {
        boolean hasSubUnit = subUnitId != null;
        boolean hasConversionRate = unitConversionRate != null;
        if (hasSubUnit != hasConversionRate) {
            throw new CommonException(ErrorCode.PRODUCT_SUB_UNIT_CONVERSION_MISMATCH);
        }
    }

}
