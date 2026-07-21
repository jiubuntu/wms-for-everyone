package com.jiubuntu.wms.biz.productunit.application.validator;

import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.biz.productunit.infrastructure.ProductUnitRepository;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProductUnitValidator {

    private final ProductUnitRepository productUnitRepository;

    public void validateCreate(Long companyId, String name) {
        if (productUnitRepository.existsActiveByCompanyAndName(companyId, name)) {
            throw new CommonException(ErrorCode.PRODUCT_UNIT_ALREADY_EXISTS);
        }
    }

    public void validateScope(ProductUnit existing, Long expectedCompanyId) {
        Long existingCompanyId = existing.getCompany() != null ? existing.getCompany().getId() : null;
        if (!Objects.equals(existingCompanyId, expectedCompanyId)) {
            throw new CommonException(ErrorCode.COMPANY_SCOPE_VIOLATION);
        }
    }

}
