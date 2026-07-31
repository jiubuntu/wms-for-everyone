package com.jiubuntu.wms.biz.commoncode.application.dto.command;

import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import lombok.Getter;

@Getter
public class CommonCodeCreateCommand {

    private final Long companyId;
    private final CommonCodeGroup groupCode;
    private final String code;
    private final String name;
    private final int sortOrder;

    public CommonCodeCreateCommand(Long companyId, CommonCodeGroup groupCode, String code, String name, int sortOrder) {
        this.companyId = companyId;
        this.groupCode = groupCode;
        this.code = code;
        this.name = name;
        this.sortOrder = sortOrder;
    }

}
