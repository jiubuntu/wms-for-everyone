package com.jiubuntu.wms.biz.user.ui.payload.response;

import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import lombok.Getter;

@Getter
public class UserIssueResponse {

    private final Long id;
    private final String email;
    private final String name;
    private final UserRole role;
    private final UserStatus status;
    private final Long warehouseId;

    private UserIssueResponse(Long id, String email, String name, UserRole role, UserStatus status, Long warehouseId) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.status = status;
        this.warehouseId = warehouseId;
    }

    public static UserIssueResponse from(User user) {
        return new UserIssueResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getStatus(),
                user.getWarehouse() != null ? user.getWarehouse().getId() : null
        );
    }

}
