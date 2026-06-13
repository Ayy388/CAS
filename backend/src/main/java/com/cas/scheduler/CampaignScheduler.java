package com.cas.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cas.common.enums.CampaignStatus;
import com.cas.modules.campaign.entity.Campaign;
import com.cas.modules.campaign.mapper.CampaignMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CampaignScheduler {

    private final CampaignMapper campaignMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 60_000)
    public void autoUpdateCampaignStatus() {
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<Campaign> pendingToActive = Wrappers.lambdaQuery();
        pendingToActive.eq(Campaign::getStatus, CampaignStatus.PENDING.name());
        pendingToActive.le(Campaign::getStartTime, now);
        Campaign activeUpdate = new Campaign();
        activeUpdate.setStatus(CampaignStatus.ACTIVE.name());
        activeUpdate.setUpdatedAt(now);
        int activeCount = campaignMapper.update(activeUpdate, pendingToActive);
        if (activeCount > 0) {
            log.info("Scheduler: {} campaign(s) PENDING → ACTIVE", activeCount);
        }

        LambdaQueryWrapper<Campaign> activeToEnded = Wrappers.lambdaQuery();
        activeToEnded.eq(Campaign::getStatus, CampaignStatus.ACTIVE.name());
        activeToEnded.le(Campaign::getEndTime, now);
        Campaign endedUpdate = new Campaign();
        endedUpdate.setStatus(CampaignStatus.ENDED.name());
        endedUpdate.setUpdatedAt(now);
        int endedCount = campaignMapper.update(endedUpdate, activeToEnded);
        if (endedCount > 0) {
            log.info("Scheduler: {} campaign(s) ACTIVE → ENDED", endedCount);
        }

        if (activeCount > 0 || endedCount > 0) {
            try {
                redisTemplate.delete("cas:campaign:current");
            } catch (Exception e) {
                log.warn("Redis unavailable, skipping scheduler cache clear");
            }
        }
    }
}