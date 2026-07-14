package com.jiubuntu.wms.biz.user.application;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import com.jiubuntu.wms.biz.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("승인 대기 중인 기업관리자 계정이 있으면 활성화한다")
    void approveCompanyAdmin_activatesPendingUser() {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.PENDING);
        User pendingAdmin = new User(company, null, "admin@test.com", "encoded-password",
                "홍길동", "010-0000-0000", UserRole.COMPANY_ADMIN, UserStatus.PENDING);
        when(userRepository.findPendingByCompanyIdAndRole(1L, UserRole.COMPANY_ADMIN))
                .thenReturn(Optional.of(pendingAdmin));

        userService.approveCompanyAdmin(1L);

        assertThat(pendingAdmin.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("승인 대기 중인 기업관리자 계정이 없으면 아무 일도 하지 않는다")
    void approveCompanyAdmin_noPendingUser_doesNothing() {
        when(userRepository.findPendingByCompanyIdAndRole(1L, UserRole.COMPANY_ADMIN))
                .thenReturn(Optional.empty());

        userService.approveCompanyAdmin(1L);
    }

}
