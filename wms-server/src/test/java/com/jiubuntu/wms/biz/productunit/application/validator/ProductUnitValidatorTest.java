package com.jiubuntu.wms.biz.productunit.application.validator;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.biz.productunit.infrastructure.ProductUnitRepository;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductUnitValidatorTest {

    @Mock
    private ProductUnitRepository productUnitRepository;

    @InjectMocks
    private ProductUnitValidator productUnitValidator;

    private Company companyWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", id);
        return company;
    }

    @Test
    @DisplayName("이미 등록된 단위명이면 PRODUCT_UNIT_ALREADY_EXISTS 예외가 발생한다")
    void validateCreate_alreadyExists() {
        when(productUnitRepository.existsActiveByCompanyAndName(1L, "박스")).thenReturn(true);

        assertThatThrownBy(() -> productUnitValidator.validateCreate(1L, "박스"))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_UNIT_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("중복이 없으면 예외가 발생하지 않는다")
    void validateCreate_success() {
        when(productUnitRepository.existsActiveByCompanyAndName(1L, "박스")).thenReturn(false);

        assertThatCode(() -> productUnitValidator.validateCreate(1L, "박스")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("다른 회사의 단위를 건드리려 하면 COMPANY_SCOPE_VIOLATION 예외가 발생한다")
    void validateScope_otherCompany_violation() {
        ProductUnit productUnit = new ProductUnit(companyWithId(1L), "박스");

        assertThatThrownBy(() -> productUnitValidator.validateScope(productUnit, 2L))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMPANY_SCOPE_VIOLATION);
    }

    @Test
    @DisplayName("자기 회사의 단위면 예외가 발생하지 않는다")
    void validateScope_sameCompany_success() {
        ProductUnit productUnit = new ProductUnit(companyWithId(1L), "박스");

        assertThatCode(() -> productUnitValidator.validateScope(productUnit, 1L)).doesNotThrowAnyException();
    }

}
