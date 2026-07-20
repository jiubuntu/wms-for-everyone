package com.jiubuntu.wms.biz.commoncode.application.dto.result;

import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommonCodeResult {

    private final Long id;
    private final Long companyId;
    private final CommonCodeGroup groupCode;
    private final String code;
    private final String name;
    private final int sortOrder;
    private final LocalDateTime createdAt;

    public CommonCodeResult(Long id, Long companyId, CommonCodeGroup groupCode, String code, String name,
                             int sortOrder, LocalDateTime createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.groupCode = groupCode;
        this.code = code;
        this.name = name;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
    }

}
