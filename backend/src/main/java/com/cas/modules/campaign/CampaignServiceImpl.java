package com.cas.modules.campaign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.common.enums.CampaignStatus;
import com.cas.common.exception.BusinessException;
import com.cas.modules.campaign.dto.CampaignRequest;
import com.cas.modules.campaign.dto.CampaignVO;
import com.cas.modules.campaign.entity.Campaign;
import com.cas.modules.campaign.mapper.CampaignMapper;
import com.cas.modules.semester.entity.Semester;
import com.cas.modules.semester.mapper.SemesterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private final CampaignMapper campaignMapper;
    private final SemesterMapper semesterMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Page<CampaignVO> listCampaigns(int page, int pageSize) {
        Page<Campaign> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Campaign> query = Wrappers.lambdaQuery();
        query.orderByDesc(Campaign::getCreatedAt);
        Page<Campaign> result = campaignMapper.selectPage(mpPage, query);

        Page<CampaignVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::toVO).collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CampaignVO createCampaign(CampaignRequest request) {
        // Check no duplicate PENDING/ACTIVE campaign in same semester
        LambdaQueryWrapper<Campaign> check = Wrappers.lambdaQuery();
        check.eq(Campaign::getSemesterId, request.getSemesterId());
        check.in(Campaign::getStatus, CampaignStatus.PENDING.name(), CampaignStatus.ACTIVE.name());
        if (campaignMapper.selectCount(check) > 0) {
            throw new BusinessException(409, "该学期已有进行中或待开始的选课活动");
        }

        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setSemesterId(request.getSemesterId());
        campaign.setStartTime(request.getStartTime());
        campaign.setEndTime(request.getEndTime());
        campaign.setStatus(CampaignStatus.PENDING.name());
        campaign.setCreatedAt(LocalDateTime.now());
        campaign.setUpdatedAt(LocalDateTime.now());
        campaignMapper.insert(campaign);
        return toVO(campaign);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CampaignVO startCampaign(Long id) {
        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BusinessException(404, "活动不存在");
        }
        if (!CampaignStatus.PENDING.name().equals(campaign.getStatus())) {
            throw new BusinessException(409, "只有待开始的活动才能开启");
        }
        campaign.setStatus(CampaignStatus.ACTIVE.name());
        campaign.setUpdatedAt(LocalDateTime.now());
        campaignMapper.updateById(campaign);
        clearCampaignCache();
        return toVO(campaign);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CampaignVO endCampaign(Long id) {
        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BusinessException(404, "活动不存在");
        }
        if (!CampaignStatus.ACTIVE.name().equals(campaign.getStatus())) {
            throw new BusinessException(409, "只有进行中的活动才能结束");
        }
        campaign.setStatus(CampaignStatus.ENDED.name());
        campaign.setUpdatedAt(LocalDateTime.now());
        campaignMapper.updateById(campaign);
        clearCampaignCache();
        return toVO(campaign);
    }

    @Override
    public CampaignVO getCurrentActive() {
        // Redis cache check (fault-tolerant)
        String cacheKey = "cas:campaign:current";
        try {
            CampaignVO cached = (CampaignVO) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache read for getCurrentActive");
        }

        // DB query
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Campaign> query = Wrappers.lambdaQuery();
        query.eq(Campaign::getStatus, CampaignStatus.ACTIVE.name());
        query.le(Campaign::getStartTime, now);
        query.ge(Campaign::getEndTime, now);
        query.last("LIMIT 1");
        Campaign campaign = campaignMapper.selectOne(query);

        if (campaign == null) {
            throw new BusinessException(404, "当前没有进行中的选课活动");
        }

        CampaignVO vo = toVO(campaign);
        try {
            redisTemplate.opsForValue().set(cacheKey, vo, 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache write for getCurrentActive");
        }
        return vo;
    }

    private void clearCampaignCache() {
        try {
            redisTemplate.delete("cas:campaign:current");
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache clear");
        }
    }

    private CampaignVO toVO(Campaign campaign) {
        CampaignVO vo = new CampaignVO();
        vo.setId(campaign.getId());
        vo.setName(campaign.getName());
        vo.setSemesterId(campaign.getSemesterId());
        vo.setStartTime(campaign.getStartTime());
        vo.setEndTime(campaign.getEndTime());
        vo.setStatus(campaign.getStatus());
        vo.setCreatedAt(campaign.getCreatedAt());
        if (campaign.getSemesterId() != null) {
            Semester semester = semesterMapper.selectById(campaign.getSemesterId());
            if (semester != null) {
                vo.setSemesterName(semester.getName());
            }
        }
        return vo;
    }
}