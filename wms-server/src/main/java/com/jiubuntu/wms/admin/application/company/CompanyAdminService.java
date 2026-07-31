package com.jiubuntu.wms.admin.application.company;

import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.application.dto.result.CompanyResult;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyAdminService {

    private final CompanyService companyService;
    private final UserService userService;

    public Page<CompanyResult> findPendingList(String keyword, Pageable pageable) {
        return companyService.findPendingList(keyword, pageable);
    }

    public Company getDetail(Long companyId) {
        return companyService.getActiveById(companyId);
    }

    public String getBusinessLicenseFileUrl(Long companyId) {
        return companyService.getBusinessLicenseFileUrl(companyId);
    }

    @Transactional
    public Company approve(Long companyId, Long updatedBy) {
        Company company = companyService.approve(companyId, updatedBy);
        userService.approveCompanyAdmin(companyId);
        return company;
    }

}
