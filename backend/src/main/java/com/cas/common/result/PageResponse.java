package com.cas.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {

    private List<T> items;
    private long total;
    private long page;
    private long pageSize;

    public PageResponse(List<T> items, long total, long page, long pageSize) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public static <T> PageResponse<T> of(IPage<T> page) {
        return new PageResponse<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }
}