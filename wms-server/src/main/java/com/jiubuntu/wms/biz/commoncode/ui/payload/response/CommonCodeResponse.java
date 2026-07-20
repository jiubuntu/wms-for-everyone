package com.jiubuntu.wms.biz.commoncode.ui.payload.response;

import com.jiubuntu.wms.biz.commoncode.application.dto.result.CommonCodeResult;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommonCodeResponse {

    private final Long id;
    private final Long companyId;
    private final CommonCodeGroup groupCode;
    private final String code;
    private final String name;
    private final int sortOrder;
    private final LocalDateTime createdAt;

    private CommonCodeResponse(Long id, Long companyId, CommonCodeGroup groupCode, String code, String name,
                                int sortOrder, LocalDateTime createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.groupCode = groupCode;
        this.code = code;
        this.name = name;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
    }

    public static CommonCodeResponse from(CommonCodeResult result) {
        return new CommonCodeResponse(
                result.getId(),
                result.getCompanyId(),
                result.getGroupCode(),
                result.getCode(),
                result.getName(),
                result.getSortOrder(),
                result.getCreatedAt()
        );
    }

    public static CommonCodeResponse from(CommonCode commonCode) {
        return new CommonCodeResponse(
                commonCode.getId(),
                commonCode.getCompany() != null ? commonCode.getCompany().getId() : null,
                commonCode.getGroupCode(),
                commonCode.getCode(),
                commonCode.getName(),
                commonCode.getSortOrder(),
                commonCode.getCreatedAt()
        );
    }

}
