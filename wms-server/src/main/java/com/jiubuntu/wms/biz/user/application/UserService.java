package com.jiubuntu.wms.biz.user.application;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.user.application.dto.command.UserChangePasswordCommand;
import com.jiubuntu.wms.biz.user.application.dto.command.UserIssueCommand;
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

    @Transactional
    public User create(Company company, String email, String encodedPassword, String name, String phone) {
        User user = new User(company, null, email, encodedPassword, name, phone, UserRole.COMPANY_ADMIN, UserStatus.PENDING);
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
    public User issueAccount(UserIssueCommand command) {
        User issuer = getActiveById(command.getIssuerUserId());
        Warehouse warehouse = warehouseService.getActiveById(command.getWarehouseId());

        userIssueValidator.validate(issuer, warehouse, command.getRole(), existsByEmail(command.getEmail()));

        User user = new User(
                issuer.getCompany(),
                warehouse,
                command.getEmail(),
                passwordEncoder.encode(command.getPassword()),
                command.getName(),
                command.getPhone(),
                command.getRole(),
                UserStatus.ACTIVE
        );
        user.assignCreator(issuer.getId());

        return userRepository.save(user);
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
