package com.jiubuntu.wms.biz.company.application.dto.result;

import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CompanyResult {

    private final Long id;
    private final String name;
    private final String businessNumber;
    private final CompanyStatus status;
    private final LocalDateTime createdAt;

    public CompanyResult(Long id, String name, String businessNumber, CompanyStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.businessNumber = businessNumber;
        this.status = status;
        this.createdAt = createdAt;
    }

}
