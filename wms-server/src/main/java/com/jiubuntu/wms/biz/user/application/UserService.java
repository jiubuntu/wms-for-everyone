package com.jiubuntu.wms.biz.user.application;

import com.jiubuntu.wms.biz.company.domain.Company;
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
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final WarehouseService warehouseService;
    private final UserIssueValidator userIssueValidator;
    private final UserWithdrawValidator userWithdrawValidator;
    private final UserChangePasswordValidator userChangePasswordValidator;
    private final PasswordEncoder passwordEncoder;
    private final InitialPasswordGenerator initialPasswordGenerator;

    @Transactional
    public User create(Company company, String email, String encodedPassword, String name, String phone) {
        User user = new User(company, null, email, encodedPassword, name, phone, UserRole.COMPANY_ADMIN, UserStatus.PENDING, false);
        return userRepository.save(user);
    }

    public Optional<User> findActiveByEmail(String email) {
        return userRepository.findActiveByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsActiveByEmail(email);
    }

    public User getActiveById(Long id) {
        return userRepository.findActiveById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void approveCompanyAdmin(Long companyId) {
        userRepository.findPendingByCompanyIdAndRole(companyId, UserRole.COMPANY_ADMIN)
                .ifPresent(User::approve);
    }

    @Transactional
    public UserIssueResult issueAccount(UserIssueCommand command) {
        User issuer = getActiveById(command.getIssuerUserId());
        Warehouse warehouse = warehouseService.getActiveById(command.getWarehouseId());

        userIssueValidator.validate(issuer, warehouse, command.getRole(), existsByEmail(command.getEmail()));

        String temporaryPassword = initialPasswordGenerator.generate();
        User user = new User(
                issuer.getCompany(),
                warehouse,
                command.getEmail(),
                passwordEncoder.encode(temporaryPassword),
                command.getName(),
                command.getPhone(),
                command.getRole(),
                UserStatus.ACTIVE,
                true
        );
        user.assignCreator(issuer.getId());
        User saved = userRepository.save(user);

        return new UserIssueResult(saved, temporaryPassword);
    }

    public Page<UserListResult> list(Long companyId, UserRole role, Long principalWarehouseId, Pageable pageable) {
        Long warehouseId = role == UserRole.WAREHOUSE_MANAGER ? principalWarehouseId : null;
        return userRepository.findActiveStaffByCompany(companyId, warehouseId, pageable)
                .map(this::toListResult);
    }

    private UserListResult toListResult(User user) {
        return new UserListResult(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getWarehouse() != null ? user.getWarehouse().getId() : null,
                user.getWarehouse() != null ? user.getWarehouse().getName() : null,
                user.getCreatedAt()
        );
    }

    @Transactional
    public User withdraw(Long requesterUserId, Long targetUserId) {
        User requester = getActiveById(requesterUserId);
        User target = getActiveById(targetUserId);

        long remainingCompanyAdminCount = userRepository.countActiveByCompanyIdAndRole(target.getCompany().getId(), UserRole.COMPANY_ADMIN);
        userWithdrawValidator.validate(requester, target, remainingCompanyAdminCount);

        target.withdraw();
        target.assignUpdater(requester.getId());

        return target;
    }

    @Transactional
    public User withdrawByAdmin(Long targetUserId, Long adminUserId) {
        User target = getActiveById(targetUserId);

        long remainingCompanyAdminCount = userRepository.countActiveByCompanyIdAndRole(target.getCompany().getId(), UserRole.COMPANY_ADMIN);
        if (target.getRole() == UserRole.COMPANY_ADMIN && remainingCompanyAdminCount <= 1) {
            throw new CommonException(ErrorCode.LAST_COMPANY_ADMIN_CANNOT_WITHDRAW);
        }

        target.withdraw();
        target.assignUpdater(adminUserId);

        return target;
    }

    @Transactional
    public User changePassword(UserChangePasswordCommand command) {
        User user = getActiveById(command.getUserId());

        userChangePasswordValidator.validate(user, command.getCurrentPassword());

        user.changePassword(passwordEncoder.encode(command.getNewPassword()));
        user.assignUpdater(user.getId());

        return user;
    }

}
