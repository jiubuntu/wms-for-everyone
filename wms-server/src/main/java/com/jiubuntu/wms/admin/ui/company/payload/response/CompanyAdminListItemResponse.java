package com.jiubuntu.wms.admin.ui.company.payload.response;

import com.jiubuntu.wms.biz.company.application.dto.result.CompanyResult;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CompanyAdminListItemResponse {

    private final Long id;
    private final String name;
    private final String businessNumber;
    private final CompanyStatus status;
    private final LocalDateTime createdAt;

    private CompanyAdminListItemResponse(Long id, String name, String businessNumber, CompanyStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.businessNumber = businessNumber;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static CompanyAdminListItemResponse from(CompanyResult result) {
        return new CompanyAdminListItemResponse(
                result.getId(),
                result.getName(),
                result.getBusinessNumber(),
                result.getStatus(),
                result.getCreatedAt()
        );
    }

}
