package com.jiubuntu.wms.biz.auth.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthPasswordResetConfirmCommand {

    private final String token;
    private final String newPassword;
    private final String newPasswordConfirm;

}
