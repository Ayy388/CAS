package com.cas.modules.campaign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.common.result.ApiResponse;
import com.cas.common.result.PageResponse;
import com.cas.modules.campaign.dto.CampaignVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    // Admin endpoints
    @GetMapping("/admin/campaigns")
    public ApiResponse<PageResponse<CampaignVO>> list(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int pageSize) {
        Page<CampaignVO> result = campaignService.listCampaigns(page, pageSize);
        return ApiResponse.success(PageResponse.of(result));
    }

    @PostMapping("/admin/campaigns")
    public ApiResponse<CampaignVO> create(@Valid @RequestBody com.cas.modules.campaign.dto.CampaignRequest request) {
        return ApiResponse.success(campaignService.createCampaign(request));
    }

    @PatchMapping("/admin/campaigns/{id}/start")
    public ApiResponse<CampaignVO> start(@PathVariable Long id) {
        return ApiResponse.success(campaignService.startCampaign(id));
    }

    @PatchMapping("/admin/campaigns/{id}/end")
    public ApiResponse<CampaignVO> end(@PathVariable Long id) {
        return ApiResponse.success(campaignService.endCampaign(id));
    }

    // Student endpoint - current active campaign
    @GetMapping("/campaigns/current")
    public ApiResponse<CampaignVO> getCurrent() {
        return ApiResponse.success(campaignService.getCurrentActive());
    }
}