package com.jiubuntu.wms.biz.user.application.dto.command;

import lombok.Getter;

@Getter
public class UserChangePasswordCommand {

    private final Long userId;
    private final String currentPassword;
    private final String newPassword;

    public UserChangePasswordCommand(Long userId, String currentPassword, String newPassword) {
        this.userId = userId;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

}
