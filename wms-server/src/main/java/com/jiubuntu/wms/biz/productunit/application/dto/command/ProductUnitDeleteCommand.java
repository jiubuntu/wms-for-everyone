package com.jiubuntu.wms.biz.productunit.application.dto.command;

import lombok.Getter;

@Getter
public class ProductUnitDeleteCommand {

    private final Long id;
    private final Long expectedCompanyId;
    private final Long updatedBy;

    public ProductUnitDeleteCommand(Long id, Long expectedCompanyId, Long updatedBy) {
        this.id = id;
        this.expectedCompanyId = expectedCompanyId;
        this.updatedBy = updatedBy;
    }

}
