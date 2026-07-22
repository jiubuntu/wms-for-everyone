package com.jiubuntu.wms.biz.product.application.validator;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.product.application.dto.command.ProductCreateCommand;
import com.jiubuntu.wms.biz.product.application.dto.command.ProductUpdateCommand;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.product.infrastructure.ProductRepository;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductValidatorTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductValidator productValidator;

    private Company companyWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", id);
        return company;
    }

    private ProductCreateCommand.ProductCreateCommandBuilder baseCreateCommand() {
        return ProductCreateCommand.builder()
                .companyId(1L)
                .skuCode("SKU-0001")
                .name("사과")
                .categoryId(1L)
                .storageTypeId(1L)
                .baseUnitId(1L);
    }

    @Test
    @DisplayName("이미 등록된 SKU 코드면 PRODUCT_ALREADY_EXISTS 예외가 발생한다")
    void validateCreate_duplicateSku() {
        when(productRepository.existsActiveByCompanyAndSkuCode(1L, "SKU-0001")).thenReturn(true);

        assertThatThrownBy(() -> productValidator.validateCreate(baseCreateCommand().build()))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("보조 단위만 있고 변환율이 없으면 PRODUCT_SUB_UNIT_CONVERSION_MISMATCH 예외가 발생한다")
    void validateCreate_subUnitWithoutRate() {
        when(productRepository.existsActiveByCompanyAndSkuCode(1L, "SKU-0001")).thenReturn(false);
        ProductCreateCommand command = baseCreateCommand().subUnitId(2L).build();

        assertThatThrownBy(() -> productValidator.validateCreate(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_SUB_UNIT_CONVERSION_MISMATCH);
    }

    @Test
    @DisplayName("변환율만 있고 보조 단위가 없으면 PRODUCT_SUB_UNIT_CONVERSION_MISMATCH 예외가 발생한다")
    void validateCreate_rateWithoutSubUnit() {
        when(productRepository.existsActiveByCompanyAndSkuCode(1L, "SKU-0001")).thenReturn(false);
        ProductCreateCommand command = baseCreateCommand().unitConversionRate(BigDecimal.valueOf(24)).build();

        assertThatThrownBy(() -> productValidator.validateCreate(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_SUB_UNIT_CONVERSION_MISMATCH);
    }

    @Test
    @DisplayName("보조 단위와 변환율이 모두 없으면 예외가 발생하지 않는다")
    void validateCreate_noSubUnit_success() {
        when(productRepository.existsActiveByCompanyAndSkuCode(1L, "SKU-0001")).thenReturn(false);

        assertThatCode(() -> productValidator.validateCreate(baseCreateCommand().build())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("보조 단위와 변환율이 모두 있으면 예외가 발생하지 않는다")
    void validateCreate_withSubUnitAndRate_success() {
        when(productRepository.existsActiveByCompanyAndSkuCode(1L, "SKU-0001")).thenReturn(false);
        ProductCreateCommand command = baseCreateCommand().subUnitId(2L).unitConversionRate(BigDecimal.valueOf(24)).build();

        assertThatCode(() -> productValidator.validateCreate(command)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수정 시에도 보조 단위/변환율 동반 여부를 검증한다")
    void validateUpdate_subUnitWithoutRate() {
        ProductUpdateCommand command = ProductUpdateCommand.builder()
                .id(1L).expectedCompanyId(1L).name("사과").categoryId(1L).storageTypeId(1L).baseUnitId(1L)
                .subUnitId(2L).updatedBy(100L).build();

        assertThatThrownBy(() -> productValidator.validateUpdate(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_SUB_UNIT_CONVERSION_MISMATCH);
    }

    @Test
    @DisplayName("다른 회사의 상품을 건드리려 하면 COMPANY_SCOPE_VIOLATION 예외가 발생한다")
    void validateScope_otherCompany_violation() {
        Product product = Product.builder().company(companyWithId(1L)).skuCode("SKU-0001").name("사과").build();

        assertThatThrownBy(() -> productValidator.validateScope(product, 2L))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMPANY_SCOPE_VIOLATION);
    }

    @Test
    @DisplayName("자기 회사의 상품이면 예외가 발생하지 않는다")
    void validateScope_sameCompany_success() {
        Product product = Product.builder().company(companyWithId(1L)).skuCode("SKU-0001").name("사과").build();

        assertThatCode(() -> productValidator.validateScope(product, 1L)).doesNotThrowAnyException();
    }

}
