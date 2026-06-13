package com.cas.modules.campaign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.modules.campaign.dto.CampaignRequest;
import com.cas.modules.campaign.dto.CampaignVO;

public interface CampaignService {
    Page<CampaignVO> listCampaigns(int page, int pageSize);
    CampaignVO createCampaign(CampaignRequest request);
    CampaignVO startCampaign(Long id);
    CampaignVO endCampaign(Long id);
    CampaignVO getCurrentActive();
}