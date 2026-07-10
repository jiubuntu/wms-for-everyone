package com.jiubuntu.wms.biz.user.application;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import com.jiubuntu.wms.biz.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

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

}
