package com.jiubuntu.wms.biz.user.application.dto.result;

import com.jiubuntu.wms.biz.user.domain.User;
import lombok.Getter;

@Getter
public class UserIssueResult {

    private final User user;
    private final String temporaryPassword;

    public UserIssueResult(User user, String temporaryPassword) {
        this.user = user;
        this.temporaryPassword = temporaryPassword;
    }

}
