package com.jiubuntu.wms.biz.product.application;

import com.jiubuntu.wms.biz.commoncode.application.CommonCodeService;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.product.application.dto.command.ProductCreateCommand;
import com.jiubuntu.wms.biz.product.application.dto.command.ProductDeleteCommand;
import com.jiubuntu.wms.biz.product.application.dto.command.ProductUpdateCommand;
import com.jiubuntu.wms.biz.product.application.validator.ProductValidator;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.product.infrastructure.ProductRepository;
import com.jiubuntu.wms.biz.productunit.application.ProductUnitService;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductValidator productValidator;

    @Mock
    private CompanyService companyService;

    @Mock
    private CommonCodeService commonCodeService;

    @Mock
    private ProductUnitService productUnitService;

    @InjectMocks
    private ProductService productService;

    private Company companyWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", id);
        return company;
    }

    private CommonCode commonCodeWithId(Long id, CommonCodeGroup group) {
        CommonCode commonCode = new CommonCode(null, group, "CODE", "이름", 1);
        ReflectionTestUtils.setField(commonCode, "id", id);
        return commonCode;
    }

    private ProductUnit productUnitWithId(Long id) {
        ProductUnit productUnit = new ProductUnit(companyWithId(1L), "단위");
        ReflectionTestUtils.setField(productUnit, "id", id);
        return productUnit;
    }

    @Test
    @DisplayName("보조 단위 없이 상품을 등록하면 보조 단위 조회는 호출되지 않는다")
    void create_withoutSubUnit_doesNotResolveSubUnit() {
        when(companyService.getActiveById(1L)).thenReturn(companyWithId(1L));
        when(commonCodeService.getAccessible(10L, CommonCodeGroup.PRODUCT_CATEGORY, 1L)).thenReturn(commonCodeWithId(10L, CommonCodeGroup.PRODUCT_CATEGORY));
        when(commonCodeService.getAccessible(20L, CommonCodeGroup.STORAGE_TYPE, 1L)).thenReturn(commonCodeWithId(20L, CommonCodeGroup.STORAGE_TYPE));
        when(productUnitService.getAccessible(30L, 1L)).thenReturn(productUnitWithId(30L));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductCreateCommand command = ProductCreateCommand.builder()
                .companyId(1L).skuCode("SKU-0001").name("사과")
                .categoryId(10L).storageTypeId(20L).baseUnitId(30L)
                .build();

        Product result = productService.create(command);

        verify(productValidator).validateCreate(command);
        verify(productUnitService, times(1)).getAccessible(any(), any());
        assertThat(result.getSkuCode()).isEqualTo("SKU-0001");
        assertThat(result.getSubUnit()).isNull();
    }

    @Test
    @DisplayName("보조 단위를 지정하면 해당 단위를 조회해 연결한다")
    void create_withSubUnit_resolvesSubUnit() {
        when(companyService.getActiveById(1L)).thenReturn(companyWithId(1L));
        when(commonCodeService.getAccessible(10L, CommonCodeGroup.PRODUCT_CATEGORY, 1L)).thenReturn(commonCodeWithId(10L, CommonCodeGroup.PRODUCT_CATEGORY));
        when(commonCodeService.getAccessible(20L, CommonCodeGroup.STORAGE_TYPE, 1L)).thenReturn(commonCodeWithId(20L, CommonCodeGroup.STORAGE_TYPE));
        when(productUnitService.getAccessible(30L, 1L)).thenReturn(productUnitWithId(30L));
        when(productUnitService.getAccessible(40L, 1L)).thenReturn(productUnitWithId(40L));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductCreateCommand command = ProductCreateCommand.builder()
                .companyId(1L).skuCode("SKU-0002").name("사과박스")
                .categoryId(10L).storageTypeId(20L).baseUnitId(30L)
                .subUnitId(40L).unitConversionRate(BigDecimal.valueOf(24))
                .build();

        Product result = productService.create(command);

        assertThat(result.getSubUnit()).isNotNull();
        assertThat(result.getUnitConversionRate()).isEqualByComparingTo(BigDecimal.valueOf(24));
    }

    @Test
    @DisplayName("상품을 수정하면 이름과 참조가 갱신되고 스코프 검증을 거친다")
    void update_success() {
        Product product = Product.builder().company(companyWithId(1L)).skuCode("SKU-0001").name("사과").build();
        when(productRepository.findActiveById(100L)).thenReturn(Optional.of(product));
        when(commonCodeService.getAccessible(10L, CommonCodeGroup.PRODUCT_CATEGORY, 1L)).thenReturn(commonCodeWithId(10L, CommonCodeGroup.PRODUCT_CATEGORY));
        when(commonCodeService.getAccessible(20L, CommonCodeGroup.STORAGE_TYPE, 1L)).thenReturn(commonCodeWithId(20L, CommonCodeGroup.STORAGE_TYPE));
        when(productUnitService.getAccessible(30L, 1L)).thenReturn(productUnitWithId(30L));

        ProductUpdateCommand command = ProductUpdateCommand.builder()
                .id(100L).expectedCompanyId(1L).name("청사과")
                .categoryId(10L).storageTypeId(20L).baseUnitId(30L)
                .updatedBy(999L)
                .build();
        Product result = productService.update(command);

        verify(productValidator).validateScope(product, 1L);
        assertThat(result.getName()).isEqualTo("청사과");
        assertThat(result.getUpdatedBy()).isEqualTo(999L);
    }

    @Test
    @DisplayName("존재하지 않는 상품을 수정하려 하면 PRODUCT_NOT_FOUND 예외가 발생한다")
    void update_notFound() {
        when(productRepository.findActiveById(100L)).thenReturn(Optional.empty());

        ProductUpdateCommand command = ProductUpdateCommand.builder()
                .id(100L).expectedCompanyId(1L).name("청사과")
                .categoryId(10L).storageTypeId(20L).baseUnitId(30L)
                .updatedBy(999L)
                .build();

        assertThatThrownBy(() -> productService.update(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("상품을 삭제하면 소프트 삭제(active=false) 처리된다")
    void delete_softDeletes() {
        Product product = Product.builder().company(companyWithId(1L)).skuCode("SKU-0001").name("사과").build();
        when(productRepository.findActiveById(100L)).thenReturn(Optional.of(product));

        ProductDeleteCommand command = new ProductDeleteCommand(100L, 1L, 999L);
        productService.delete(command);

        verify(productValidator).validateScope(product, 1L);
        assertThat(product.isActive()).isFalse();
        assertThat(product.getUpdatedBy()).isEqualTo(999L);
    }

}
