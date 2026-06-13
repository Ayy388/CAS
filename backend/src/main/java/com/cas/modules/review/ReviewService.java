package com.cas.modules.review;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.common.result.PageResponse;

public interface ReviewService {
    Page<?> listReviews(int page, int pageSize);
    void approve(Long offeringId);
    void reject(Long offeringId);
}