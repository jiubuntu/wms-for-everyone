package com.jiubuntu.wms.biz.commoncode.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonCodeGroup {

    PRODUCT_CATEGORY(true),
    STORAGE_TYPE(false),
    TRANSFER_REASON(false),
    ;

    private final boolean customizable;



}
