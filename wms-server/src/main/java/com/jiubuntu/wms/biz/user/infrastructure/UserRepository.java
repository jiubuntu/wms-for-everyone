package com.jiubuntu.wms.biz.user.infrastructure;

import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.infrastructure.custom.CustomUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {
}
