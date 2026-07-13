package com.jiubuntu.wms.admin.ui.company.payload.response;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import lombok.Getter;

@Getter
public class CompanyAdminApproveResponse {

    private final Long id;
    private final CompanyStatus status;

    private CompanyAdminApproveResponse(Long id, CompanyStatus status) {
        this.id = id;
        this.status = status;
    }

    public static CompanyAdminApproveResponse from(Company company) {
        return new CompanyAdminApproveResponse(company.getId(), company.getStatus());
    }

}
