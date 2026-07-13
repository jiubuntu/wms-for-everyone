package com.jiubuntu.wms.biz.user.application.dto.command;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import lombok.Getter;

@Getter
public class UserIssueCommand {

    private final Long issuerUserId;
    private final String email;
    private final String password;
    private final String name;
    private final String phone;
    private final UserRole role;
    private final Long warehouseId;

    public UserIssueCommand(Long issuerUserId, String email, String password, String name, String phone,
                             UserRole role, Long warehouseId) {
        this.issuerUserId = issuerUserId;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.warehouseId = warehouseId;
    }

}
