package com.jiubuntu.wms.biz.productunit.application.dto.command;

import lombok.Getter;

@Getter
public class ProductUnitUpdateCommand {

    private final Long id;
    private final Long expectedCompanyId;
    private final String name;
    private final Long updatedBy;

    public ProductUnitUpdateCommand(Long id, Long expectedCompanyId, String name, Long updatedBy) {
        this.id = id;
        this.expectedCompanyId = expectedCompanyId;
        this.name = name;
        this.updatedBy = updatedBy;
    }

}
