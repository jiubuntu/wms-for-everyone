package com.jiubuntu.wms.biz.commoncode.application.dto.command;

import lombok.Getter;

@Getter
public class CommonCodeUpdateCommand {

    private final Long id;
    private final Long expectedCompanyId;
    private final String name;
    private final int sortOrder;
    private final Long updatedBy;

    public CommonCodeUpdateCommand(Long id, Long expectedCompanyId, String name, int sortOrder, Long updatedBy) {
        this.id = id;
        this.expectedCompanyId = expectedCompanyId;
        this.name = name;
        this.sortOrder = sortOrder;
        this.updatedBy = updatedBy;
    }

}
