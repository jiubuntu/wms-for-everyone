package com.jiubuntu.wms.biz.product.application.dto.command;

import lombok.Getter;

@Getter
public class ProductDeleteCommand {

    private final Long id;
    private final Long expectedCompanyId;
    private final Long updatedBy;

    public ProductDeleteCommand(Long id, Long expectedCompanyId, Long updatedBy) {
        this.id = id;
        this.expectedCompanyId = expectedCompanyId;
        this.updatedBy = updatedBy;
    }

}
