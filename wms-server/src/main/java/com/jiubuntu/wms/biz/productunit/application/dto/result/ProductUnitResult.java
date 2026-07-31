package com.jiubuntu.wms.biz.productunit.application.dto.result;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductUnitResult {

    private final Long id;
    private final Long companyId;
    private final String name;
    private final LocalDateTime createdAt;

    public ProductUnitResult(Long id, Long companyId, String name, LocalDateTime createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.createdAt = createdAt;
    }

}
