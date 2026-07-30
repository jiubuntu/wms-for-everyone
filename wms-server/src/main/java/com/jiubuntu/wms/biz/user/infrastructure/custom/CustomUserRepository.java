package com.jiubuntu.wms.biz.user.infrastructure.custom;

import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomUserRepository {

    Optional<User> findActiveByEmail(String email);

    boolean existsActiveByEmail(String email);

    Optional<User> findActiveById(Long id);

    long countActiveByCompanyIdAndRole(Long companyId, UserRole role);

    Optional<User> findPendingByCompanyIdAndRole(Long companyId, UserRole role);

    boolean existsActiveByWarehouseId(Long warehouseId);

    Page<User> findActiveStaffByCompany(Long companyId, Long warehouseId, String keyword, Pageable pageable);

}
