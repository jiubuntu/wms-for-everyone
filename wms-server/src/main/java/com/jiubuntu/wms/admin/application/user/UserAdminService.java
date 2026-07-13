package com.jiubuntu.wms.admin.application.user;

import com.jiubuntu.wms.biz.user.application.UserService;
import com.jiubuntu.wms.biz.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminService {

    private final UserService userService;

    @Transactional
    public User withdraw(Long targetUserId, Long adminUserId) {
        return userService.withdrawByAdmin(targetUserId, adminUserId);
    }

}
