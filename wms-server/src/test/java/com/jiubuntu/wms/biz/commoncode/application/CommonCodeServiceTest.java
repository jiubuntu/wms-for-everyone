package com.jiubuntu.wms.biz.commoncode.application;

import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeCreateCommand;
import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeDeleteCommand;
import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeUpdateCommand;
import com.jiubuntu.wms.biz.commoncode.application.validator.CommonCodeValidator;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.commoncode.infrastructure.CommonCodeRepository;
import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommonCodeServiceTest {

    @Mock
    private CommonCodeRepository commonCodeRepository;

    @Mock
    private CommonCodeValidator commonCodeValidator;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private CommonCodeService commonCodeService;

    private Company companyWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", id);
        return company;
    }

    @Test
    @DisplayName("회사 커스텀 코드를 등록하면 해당 회사를 조회해 FK로 연결한다")
    void create_companyCode_resolvesCompany() {
        Company company = companyWithId(1L);
        when(companyService.getActiveById(1L)).thenReturn(company);
        when(commonCodeRepository.save(any(CommonCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommonCodeCreateCommand command = new CommonCodeCreateCommand(1L, CommonCodeGroup.PRODUCT_CATEGORY, "ELECTRONICS", "전자제품", 1);
        CommonCode result = commonCodeService.create(command);

        verify(commonCodeValidator).validateCreate(command);
        assertThat(result.getCompany()).isEqualTo(company);
        assertThat(result.getCode()).isEqualTo("ELECTRONICS");
    }

    @Test
    @DisplayName("시스템 코드를 등록하면 회사를 조회하지 않는다")
    void create_systemCode_doesNotResolveCompany() {
        when(commonCodeRepository.save(any(CommonCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommonCodeCreateCommand command = new CommonCodeCreateCommand(null, CommonCodeGroup.STORAGE_TYPE, "COLD", "냉장", 1);
        CommonCode result = commonCodeService.create(command);

        verify(companyService, never()).getActiveById(any());
        assertThat(result.getCompany()).isNull();
    }

    @Test
    @DisplayName("코드를 수정하면 이름과 정렬 순서가 반영되고 스코프 검증을 거친다")
    void update_success() {
        CommonCode commonCode = new CommonCode(companyWithId(1L), CommonCodeGroup.PRODUCT_CATEGORY, "ELECTRONICS", "전자제품", 1);
        when(commonCodeRepository.findActiveById(10L)).thenReturn(Optional.of(commonCode));

        CommonCodeUpdateCommand command = new CommonCodeUpdateCommand(10L, 1L, "전자기기", 2, 100L);
        CommonCode result = commonCodeService.update(command);

        verify(commonCodeValidator).validateScope(commonCode, 1L);
        assertThat(result.getName()).isEqualTo("전자기기");
        assertThat(result.getSortOrder()).isEqualTo(2);
        assertThat(result.getUpdatedBy()).isEqualTo(100L);
    }

    @Test
    @DisplayName("존재하지 않는 코드를 수정하려 하면 COMMON_CODE_NOT_FOUND 예외가 발생한다")
    void update_notFound() {
        when(commonCodeRepository.findActiveById(10L)).thenReturn(Optional.empty());

        CommonCodeUpdateCommand command = new CommonCodeUpdateCommand(10L, 1L, "전자기기", 2, 100L);

        assertThatThrownBy(() -> commonCodeService.update(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMMON_CODE_NOT_FOUND);
    }

    @Test
    @DisplayName("코드를 삭제하면 소프트 삭제(active=false) 처리된다")
    void delete_softDeletes() {
        CommonCode commonCode = new CommonCode(companyWithId(1L), CommonCodeGroup.PRODUCT_CATEGORY, "ELECTRONICS", "전자제품", 1);
        when(commonCodeRepository.findActiveById(10L)).thenReturn(Optional.of(commonCode));

        CommonCodeDeleteCommand command = new CommonCodeDeleteCommand(10L, 1L, 100L);
        commonCodeService.delete(command);

        verify(commonCodeValidator).validateScope(commonCode, 1L);
        assertThat(commonCode.isActive()).isFalse();
        assertThat(commonCode.getUpdatedBy()).isEqualTo(100L);
    }

}
