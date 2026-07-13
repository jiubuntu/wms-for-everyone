package com.jiubuntu.wms.biz.user.application.validator;

import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class UserWithdrawValidator {

    public void validate(User requester, User target, long remainingCompanyAdminCount) {
        if (requester.getId().equals(target.getId())) {
            validateLastCompanyAdmin(target, remainingCompanyAdminCount);
            return;
        }

        if (requester.getRole() == UserRole.COMPANY_ADMIN) {
            validateCompanyAdminWithdraw(requester, target);
            return;
        }

        if (requester.getRole() == UserRole.WAREHOUSE_MANAGER) {
            validateWarehouseManagerWithdraw(requester, target);
            return;
        }

        throw new CommonException(ErrorCode.FORBIDDEN);
    }

    private void validateCompanyAdminWithdraw(User requester, User target) {
        if (target.getRole() != UserRole.WAREHOUSE_MANAGER && target.getRole() != UserRole.WORKER) {
            throw new CommonException(ErrorCode.FORBIDDEN);
        }

        if (!target.getCompany().getId().equals(requester.getCompany().getId())) {
            throw new CommonException(ErrorCode.COMPANY_SCOPE_VIOLATION);
        }
    }

    private void validateWarehouseManagerWithdraw(User requester, User target) {
        if (target.getRole() != UserRole.WORKER) {
            throw new CommonException(ErrorCode.FORBIDDEN);
        }

        if (requester.getWarehouse() == null || target.getWarehouse() == null
                || !requester.getWarehouse().getId().equals(target.getWarehouse().getId())) {
            throw new CommonException(ErrorCode.WAREHOUSE_SCOPE_VIOLATION);
        }
    }

    private void validateLastCompanyAdmin(User target, long remainingCompanyAdminCount) {
        if (target.getRole() == UserRole.COMPANY_ADMIN && remainingCompanyAdminCount <= 1) {
            throw new CommonException(ErrorCode.LAST_COMPANY_ADMIN_CANNOT_WITHDRAW);
        }
    }

}
