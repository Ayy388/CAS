package com.cas.modules.offering;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.modules.offering.dto.OfferingRequest;
import com.cas.modules.offering.dto.OfferingVO;

public interface OfferingService {
    Page<OfferingVO> listOfferings(int page, int pageSize);
    OfferingVO createOffering(OfferingRequest request);
    OfferingVO updateOffering(Long id, OfferingRequest request);
    Page<OfferingVO> listStudentOfferings(int page, int pageSize, String keyword, String type);
    OfferingVO getStudentOfferingDetail(Long id);
}