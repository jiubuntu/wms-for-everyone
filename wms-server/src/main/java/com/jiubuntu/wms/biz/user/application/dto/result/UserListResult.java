package com.jiubuntu.wms.biz.user.application.dto.result;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserListResult {

    private final Long id;
    private final String email;
    private final String name;
    private final String phone;
    private final UserRole role;
    private final UserStatus status;
    private final Long warehouseId;
    private final String warehouseName;
    private final LocalDateTime createdAt;

    public UserListResult(Long id, String email, String name, String phone, UserRole role, UserStatus status,
                           Long warehouseId, String warehouseName, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.createdAt = createdAt;
    }

}
