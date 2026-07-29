package com.jiubuntu.wms.biz.user.ui.payload.response;

import com.jiubuntu.wms.biz.user.application.dto.result.UserListResult;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserListResponse {

    private final Long id;
    private final String email;
    private final String name;
    private final String phone;
    private final UserRole role;
    private final UserStatus status;
    private final Long warehouseId;
    private final String warehouseName;
    private final LocalDateTime createdAt;

    private UserListResponse(Long id, String email, String name, String phone, UserRole role, UserStatus status,
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

    public static UserListResponse from(UserListResult result) {
        return new UserListResponse(
                result.getId(),
                result.getEmail(),
                result.getName(),
                result.getPhone(),
                result.getRole(),
                result.getStatus(),
                result.getWarehouseId(),
                result.getWarehouseName(),
                result.getCreatedAt()
        );
    }

}
