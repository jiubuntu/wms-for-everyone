package com.jiubuntu.wms.global.payload.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;

@Getter
public class ApiPageResponse<T> {
    private final Collection<T> content;
    private final PageInfo pageInfo;

    private ApiPageResponse(List<T> content, Page page) {
        this.content = content;
        this.pageInfo = new PageInfo(page);
    }

    public static <T> ApiPageResponse<T> of(Page<T> page) {
        return new ApiPageResponse<>(
                page.getContent(),
                page
        );
    }

}
