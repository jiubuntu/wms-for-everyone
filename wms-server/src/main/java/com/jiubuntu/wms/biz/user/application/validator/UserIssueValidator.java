package com.jiubuntu.wms.biz.user.application.validator;

import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class UserIssueValidator {

    public void validate(User issuer, Warehouse warehouse, UserRole targetRole, boolean emailAlreadyExists) {
        validateIssuerRole(issuer.getRole(), targetRole);
        validateWarehouseScope(issuer, warehouse);

        if (emailAlreadyExists) {
            throw new CommonException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private void validateIssuerRole(UserRole issuerRole, UserRole targetRole) {
        if (issuerRole == UserRole.COMPANY_ADMIN) {
            if (targetRole != UserRole.WAREHOUSE_MANAGER && targetRole != UserRole.WORKER) {
                throw new CommonException(ErrorCode.FORBIDDEN);
            }
            return;
        }

        if (issuerRole == UserRole.WAREHOUSE_MANAGER) {
            if (targetRole != UserRole.WORKER) {
                throw new CommonException(ErrorCode.FORBIDDEN);
            }
            return;
        }

        throw new CommonException(ErrorCode.FORBIDDEN);
    }

    private void validateWarehouseScope(User issuer, Warehouse warehouse) {
        if (!warehouse.getCompany().getId().equals(issuer.getCompany().getId())) {
            throw new CommonException(ErrorCode.COMPANY_SCOPE_VIOLATION);
        }

        if (issuer.getRole() == UserRole.WAREHOUSE_MANAGER
                && (issuer.getWarehouse() == null || !issuer.getWarehouse().getId().equals(warehouse.getId()))) {
            throw new CommonException(ErrorCode.WAREHOUSE_SCOPE_VIOLATION);
        }
    }

}
