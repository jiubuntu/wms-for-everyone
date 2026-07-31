package com.jiubuntu.wms.biz.productunit.application;

import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.productunit.application.dto.command.ProductUnitDeleteCommand;
import com.jiubuntu.wms.biz.productunit.application.dto.command.ProductUnitUpdateCommand;
import com.jiubuntu.wms.biz.productunit.application.validator.ProductUnitValidator;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductUnitServiceTest {

    @Mock
    private ProductUnitRepository productUnitRepository;

    @Mock
    private ProductUnitValidator productUnitValidator;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private ProductUnitService productUnitService;

    private Company companyWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", id);
        return company;
    }

    @Test
    @DisplayName("단위를 등록하면 해당 회사를 조회해 FK로 연결한다")
    void create_resolvesCompany() {
        Company company = companyWithId(1L);
        when(companyService.getActiveById(1L)).thenReturn(company);
        when(productUnitRepository.save(any(ProductUnit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductUnit result = productUnitService.create(1L, "박스");

        verify(productUnitValidator).validateCreate(1L, "박스");
        assertThat(result.getCompany()).isEqualTo(company);
        assertThat(result.getName()).isEqualTo("박스");
    }

    @Test
    @DisplayName("단위를 수정하면 이름이 반영되고 스코프 검증을 거친다")
    void update_success() {
        ProductUnit productUnit = new ProductUnit(companyWithId(1L), "박스");
        when(productUnitRepository.findActiveById(10L)).thenReturn(Optional.of(productUnit));

        ProductUnitUpdateCommand command = new ProductUnitUpdateCommand(10L, 1L, "카톤박스", 100L);
        ProductUnit result = productUnitService.update(command);

        verify(productUnitValidator).validateScope(productUnit, 1L);
        assertThat(result.getName()).isEqualTo("카톤박스");
        assertThat(result.getUpdatedBy()).isEqualTo(100L);
    }

    @Test
    @DisplayName("존재하지 않는 단위를 수정하려 하면 PRODUCT_UNIT_NOT_FOUND 예외가 발생한다")
    void update_notFound() {
        when(productUnitRepository.findActiveById(10L)).thenReturn(Optional.empty());

        ProductUnitUpdateCommand command = new ProductUnitUpdateCommand(10L, 1L, "카톤박스", 100L);

        assertThatThrownBy(() -> productUnitService.update(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_UNIT_NOT_FOUND);
    }

    @Test
    @DisplayName("단위를 삭제하면 소프트 삭제(active=false) 처리된다")
    void delete_softDeletes() {
        ProductUnit productUnit = new ProductUnit(companyWithId(1L), "박스");
        when(productUnitRepository.findActiveById(10L)).thenReturn(Optional.of(productUnit));

        ProductUnitDeleteCommand command = new ProductUnitDeleteCommand(10L, 1L, 100L);
        productUnitService.delete(command);

        verify(productUnitValidator).validateScope(productUnit, 1L);
        assertThat(productUnit.isActive()).isFalse();
        assertThat(productUnit.getUpdatedBy()).isEqualTo(100L);
    }

}
