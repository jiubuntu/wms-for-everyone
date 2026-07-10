package com.jiubuntu.wms.global.security;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import lombok.Getter;

@Getter
public class AuthPrincipal {

    private final Long userId;
    private final Long companyId;
    private final UserRole role;

    public AuthPrincipal(Long userId, Long companyId, UserRole role) {
        this.userId = userId;
        this.companyId = companyId;
        this.role = role;
    }

}
