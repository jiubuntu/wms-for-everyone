package com.jiubuntu.wms.biz.commoncode.application.validator;

import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeCreateCommand;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.commoncode.infrastructure.CommonCodeRepository;
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommonCodeValidatorTest {

    @Mock
    private CommonCodeRepository commonCodeRepository;

    @InjectMocks
    private CommonCodeValidator commonCodeValidator;

    private Company companyWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", id);
        return company;
    }

    @Test
    @DisplayName("커스텀 불가 그룹에 회사 코드로 등록하면 COMMON_CODE_GROUP_NOT_CUSTOMIZABLE 예외가 발생한다")
    void validateCreate_groupNotCustomizable() {
        CommonCodeCreateCommand command = new CommonCodeCreateCommand(1L, CommonCodeGroup.STORAGE_TYPE, "COLD", "냉장", 1);

        assertThatThrownBy(() -> commonCodeValidator.validateCreate(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMMON_CODE_GROUP_NOT_CUSTOMIZABLE);
    }

    @Test
    @DisplayName("이미 등록된 코드면 COMMON_CODE_ALREADY_EXISTS 예외가 발생한다")
    void validateCreate_alreadyExists() {
        CommonCodeCreateCommand command = new CommonCodeCreateCommand(1L, CommonCodeGroup.PRODUCT_CATEGORY, "ELECTRONICS", "전자제품", 1);
        when(commonCodeRepository.existsActiveByCompanyAndGroupAndCode(1L, CommonCodeGroup.PRODUCT_CATEGORY, "ELECTRONICS"))
                .thenReturn(true);

        assertThatThrownBy(() -> commonCodeValidator.validateCreate(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMMON_CODE_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("시스템 코드는 커스텀 불가 그룹이어도 등록할 수 있다")
    void validateCreate_systemCode_anyGroup_success() {
        CommonCodeCreateCommand command = new CommonCodeCreateCommand(null, CommonCodeGroup.STORAGE_TYPE, "COLD", "냉장", 1);
        when(commonCodeRepository.existsActiveByCompanyAndGroupAndCode(null, CommonCodeGroup.STORAGE_TYPE, "COLD"))
                .thenReturn(false);

        assertThatCode(() -> commonCodeValidator.validateCreate(command)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("커스텀 허용 그룹에 회사 코드로 등록하고 중복이 없으면 예외가 발생하지 않는다")
    void validateCreate_success() {
        CommonCodeCreateCommand command = new CommonCodeCreateCommand(1L, CommonCodeGroup.PRODUCT_CATEGORY, "ELECTRONICS", "전자제품", 1);
        when(commonCodeRepository.existsActiveByCompanyAndGroupAndCode(1L, CommonCodeGroup.PRODUCT_CATEGORY, "ELECTRONICS"))
                .thenReturn(false);

        assertThatCode(() -> commonCodeValidator.validateCreate(command)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("다른 회사의 커스텀 코드를 건드리려 하면 COMPANY_SCOPE_VIOLATION 예외가 발생한다")
    void validateScope_otherCompany_violation() {
        CommonCode commonCode = new CommonCode(companyWithId(1L), CommonCodeGroup.PRODUCT_CATEGORY, "ELECTRONICS", "전자제품", 1);

        assertThatThrownBy(() -> commonCodeValidator.validateScope(commonCode, 2L))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMPANY_SCOPE_VIOLATION);
    }

    @Test
    @DisplayName("기업관리자가 시스템 코드를 건드리려 하면 COMPANY_SCOPE_VIOLATION 예외가 발생한다")
    void validateScope_companyAdminTouchesSystemCode_violation() {
        CommonCode commonCode = new CommonCode(null, CommonCodeGroup.STORAGE_TYPE, "COLD", "냉장", 1);

        assertThatThrownBy(() -> commonCodeValidator.validateScope(commonCode, 1L))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMPANY_SCOPE_VIOLATION);
    }

    @Test
    @DisplayName("시스템관리자가 회사 커스텀 코드를 건드리려 하면 COMPANY_SCOPE_VIOLATION 예외가 발생한다")
    void validateScope_adminTouchesCompanyCode_violation() {
        CommonCode commonCode = new CommonCode(companyWithId(1L), CommonCodeGroup.PRODUCT_CATEGORY, "ELECTRONICS", "전자제품", 1);

        assertThatThrownBy(() -> commonCodeValidator.validateScope(commonCode, null))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMPANY_SCOPE_VIOLATION);
    }

    @Test
    @DisplayName("자기 회사의 커스텀 코드면 예외가 발생하지 않는다")
    void validateScope_sameCompany_success() {
        CommonCode commonCode = new CommonCode(companyWithId(1L), CommonCodeGroup.PRODUCT_CATEGORY, "ELECTRONICS", "전자제품", 1);

        assertThatCode(() -> commonCodeValidator.validateScope(commonCode, 1L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("시스템 코드를 시스템관리자가 건드리면 예외가 발생하지 않는다")
    void validateScope_systemCode_adminAccess_success() {
        CommonCode commonCode = new CommonCode(null, CommonCodeGroup.STORAGE_TYPE, "COLD", "냉장", 1);

        assertThatCode(() -> commonCodeValidator.validateScope(commonCode, null)).doesNotThrowAnyException();
    }

}
