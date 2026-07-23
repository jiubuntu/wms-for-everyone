package com.jiubuntu.wms.global.security.authentication;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import lombok.Getter;

@Getter
public class AuthPrincipal {

    private final Long userId;
    private final Long companyId;
    private final UserRole role;
    private final Long warehouseId;

    public AuthPrincipal(Long userId, Long companyId, UserRole role, Long warehouseId) {
        this.userId = userId;
        this.companyId = companyId;
        this.role = role;
        this.warehouseId = warehouseId;
    }

}
