package com.jiubuntu.wms.biz.user.application;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.user.application.dto.command.UserChangePasswordCommand;
import com.jiubuntu.wms.biz.user.application.dto.command.UserIssueCommand;
import com.jiubuntu.wms.biz.user.application.dto.result.UserIssueResult;
import com.jiubuntu.wms.biz.user.application.dto.result.UserListResult;
import com.jiubuntu.wms.biz.user.application.validator.UserChangePasswordValidator;
import com.jiubuntu.wms.biz.user.application.validator.UserIssueValidator;
import com.jiubuntu.wms.biz.user.application.validator.UserWithdrawValidator;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import com.jiubuntu.wms.biz.user.infrastructure.UserRepository;
import com.jiubuntu.wms.biz.warehouse.application.WarehouseService;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private UserIssueValidator userIssueValidator;

    @Mock
    private UserWithdrawValidator userWithdrawValidator;

    @Mock
    private UserChangePasswordValidator userChangePasswordValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private InitialPasswordGenerator initialPasswordGenerator;

    @InjectMocks
    private UserService userService;

    private Warehouse warehouseWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", 1L);
        Warehouse warehouse = new Warehouse(company, "테스트창고", null);
        ReflectionTestUtils.setField(warehouse, "id", id);
        return warehouse;
    }

    @Test
    @DisplayName("승인 대기 중인 기업관리자 계정이 있으면 활성화한다")
    void approveCompanyAdmin_activatesPendingUser() {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.PENDING);
        User pendingAdmin = new User(company, null, "admin@test.com", "encoded-password",
                "홍길동", "010-0000-0000", UserRole.COMPANY_ADMIN, UserStatus.PENDING, false);
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

    @Test
    @DisplayName("계정 발급 시 서버가 랜덤 비밀번호를 생성해 인코딩 저장하고, 최초 로그인 시 비밀번호 변경이 필요한 상태로 생성한다")
    void issueAccount_generatesRandomPasswordAndRequiresPasswordChange() {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", 1L);
        User issuer = new User(company, null, "admin@test.com", "encoded-admin-password",
                "관리자", "010-0000-0000", UserRole.COMPANY_ADMIN, UserStatus.ACTIVE, false);
        ReflectionTestUtils.setField(issuer, "id", 100L);
        Warehouse warehouse = warehouseWithId(10L);

        when(userRepository.findActiveById(100L)).thenReturn(Optional.of(issuer));
        when(warehouseService.getActiveById(10L)).thenReturn(warehouse);
        when(userRepository.existsActiveByEmail("worker@test.com")).thenReturn(false);
        when(initialPasswordGenerator.generate()).thenReturn("Temp123!xy");
        when(passwordEncoder.encode("Temp123!xy")).thenReturn("encoded-temp-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserIssueCommand command = new UserIssueCommand(
                100L, "worker@test.com", "김작업", "010-1111-2222", UserRole.WORKER, 10L);

        UserIssueResult result = userService.issueAccount(command);

        assertThat(result.getTemporaryPassword()).isEqualTo("Temp123!xy");
        assertThat(result.getUser().getPassword()).isEqualTo("encoded-temp-password");
        assertThat(result.getUser().isMustChangePassword()).isTrue();
        assertThat(result.getUser().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("비밀번호 변경에 성공하면 최초 로그인 비밀번호 변경 필요 플래그가 해제된다")
    void changePassword_success_clearsMustChangePasswordFlag() {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        User user = new User(company, null, "worker@test.com", "encoded-old-password",
                "김작업", "010-1111-2222", UserRole.WORKER, UserStatus.ACTIVE, true);
        ReflectionTestUtils.setField(user, "id", 200L);

        when(userRepository.findActiveById(200L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword1!")).thenReturn("encoded-new-password");

        UserChangePasswordCommand command = new UserChangePasswordCommand(200L, "oldPassword1!", "newPassword1!");
        userService.changePassword(command);

        assertThat(user.isMustChangePassword()).isFalse();
        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
    }

    @Test
    @DisplayName("기업관리자가 직원 목록을 조회하면 담당 창고 제한 없이 전사 범위로 조회한다")
    void list_companyAdmin_queriesWithoutWarehouseScope() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findActiveStaffByCompany(eq(1L), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<UserListResult> results = userService.list(1L, UserRole.COMPANY_ADMIN, null, pageable);

        assertThat(results.getContent()).isEmpty();
        verify(userRepository).findActiveStaffByCompany(1L, null, pageable);
    }

    @Test
    @DisplayName("창고관리자가 직원 목록을 조회하면 담당 창고로 스코프가 제한된다")
    void list_warehouseManager_scopedToOwnWarehouse() {
        Pageable pageable = PageRequest.of(0, 10);
        Warehouse warehouse = warehouseWithId(10L);
        Company company = warehouse.getCompany();
        User worker = new User(company, warehouse, "worker@test.com", "encoded-password",
                "김작업", "010-1111-2222", UserRole.WORKER, UserStatus.ACTIVE, false);
        ReflectionTestUtils.setField(worker, "id", 300L);
        when(userRepository.findActiveStaffByCompany(eq(1L), eq(10L), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(worker)));

        Page<UserListResult> results = userService.list(1L, UserRole.WAREHOUSE_MANAGER, 10L, pageable);

        assertThat(results.getContent()).hasSize(1);
        UserListResult result = results.getContent().get(0);
        assertThat(result.getWarehouseId()).isEqualTo(10L);
        assertThat(result.getWarehouseName()).isEqualTo("테스트창고");
        verify(userRepository).findActiveStaffByCompany(1L, 10L, pageable);
    }

}
