package com.jiubuntu.wms.biz.user.infrastructure.custom;

import com.jiubuntu.wms.biz.user.domain.User;

import java.util.Optional;

public interface CustomUserRepository {

    Optional<User> findActiveByEmail(String email);

    boolean existsActiveByEmail(String email);

}
