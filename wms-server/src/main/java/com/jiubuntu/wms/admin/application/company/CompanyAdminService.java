package com.jiubuntu.wms.admin.application.company;

import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.application.dto.result.CompanyResult;
import com.jiubuntu.wms.biz.company.domain.Company;
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

    public Page<CompanyResult> findPendingList(Pageable pageable) {
        return companyService.findPendingList(pageable);
    }

    public Company getDetail(Long companyId) {
        return companyService.getActiveById(companyId);
    }

    public String getBusinessLicenseFileUrl(Long companyId) {
        return companyService.getBusinessLicenseFileUrl(companyId);
    }

    @Transactional
    public Company approve(Long companyId, Long updatedBy) {
        return companyService.approve(companyId, updatedBy);
    }

}
