package com.jiubuntu.wms.admin.ui.company.payload.response;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CompanyAdminDetailResponse {

    private final Long id;
    private final String name;
    private final String businessNumber;
    private final CompanyStatus status;
    private final String businessLicenseFileUrl;
    private final LocalDateTime createdAt;

    private CompanyAdminDetailResponse(Long id, String name, String businessNumber, CompanyStatus status,
                                        String businessLicenseFileUrl, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.businessNumber = businessNumber;
        this.status = status;
        this.businessLicenseFileUrl = businessLicenseFileUrl;
        this.createdAt = createdAt;
    }

    public static CompanyAdminDetailResponse of(Company company, String businessLicenseFileUrl) {
        return new CompanyAdminDetailResponse(
                company.getId(),
                company.getName(),
                company.getBusinessNumber(),
                company.getStatus(),
                businessLicenseFileUrl,
                company.getCreatedAt()
        );
    }

}
