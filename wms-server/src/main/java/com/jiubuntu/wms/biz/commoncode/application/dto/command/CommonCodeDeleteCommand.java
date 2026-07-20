package com.jiubuntu.wms.biz.commoncode.application.dto.command;

import lombok.Getter;

@Getter
public class CommonCodeDeleteCommand {

    private final Long id;
    private final Long expectedCompanyId;
    private final Long updatedBy;

    public CommonCodeDeleteCommand(Long id, Long expectedCompanyId, Long updatedBy) {
        this.id = id;
        this.expectedCompanyId = expectedCompanyId;
        this.updatedBy = updatedBy;
    }

}
