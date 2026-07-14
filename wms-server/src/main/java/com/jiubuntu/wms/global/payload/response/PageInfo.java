package com.jiubuntu.wms.global.payload.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PageInfo {

    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;

    public PageInfo(Page page) {
        this.page = page.getNumber() + 1;
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.hasNext = page.hasNext();
    }
}
