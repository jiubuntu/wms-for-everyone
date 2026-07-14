package com.jiubuntu.wms.admin.application.company;

import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.user.application.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyAdminServiceTest {

    @Mock
    private CompanyService companyService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CompanyAdminService companyAdminService;

    @Test
    @DisplayName("기업 승인 시 해당 기업의 기업관리자 계정도 함께 승인한다")
    void approve_alsoApprovesCompanyAdminUser() {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        when(companyService.approve(1L, 100L)).thenReturn(company);

        Company result = companyAdminService.approve(1L, 100L);

        assertThat(result).isEqualTo(company);
        verify(userService).approveCompanyAdmin(1L);
    }

}
