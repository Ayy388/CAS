package com.cas.modules.review;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.auth.entity.User;
import com.cas.auth.mapper.UserMapper;
import com.cas.common.enums.EnrollmentStatus;
import com.cas.common.enums.OfferingStatus;
import com.cas.common.exception.BusinessException;
import com.cas.modules.campaign.entity.Campaign;
import com.cas.modules.campaign.mapper.CampaignMapper;
import com.cas.modules.course.entity.Course;
import com.cas.modules.course.mapper.CourseMapper;
import com.cas.modules.enrollment.entity.Enrollment;
import com.cas.modules.enrollment.mapper.EnrollmentMapper;
import com.cas.modules.offering.entity.Offering;
import com.cas.modules.offering.mapper.OfferingMapper;
import com.cas.mq.event.ReviewEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final OfferingMapper offeringMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final CampaignMapper campaignMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Page<?> listReviews(int page, int pageSize) {
        LambdaQueryWrapper<Offering> q = Wrappers.lambdaQuery();
        q.eq(Offering::getStatus, OfferingStatus.PENDING.name())
                .orderByDesc(Offering::getCreatedAt);
        Page<Offering> p = offeringMapper.selectPage(new Page<>(page, pageSize), q);

        List<Map<String, Object>> items = p.getRecords().stream().map(o -> {
            Map<String, Object> item = new HashMap<>();
            item.put("offeringId", o.getId());
            Course c = courseMapper.selectById(o.getCourseId());
            item.put("courseName", c != null ? c.getName() : "");
            User teacher = userMapper.selectById(o.getTeacherId());
            item.put("teacherName", teacher != null ? teacher.getRealName() : "");
            item.put("enrolledCount", o.getEnrolledCount());
            item.put("minEnrollment", o.getMinEnrollment());
            item.put("suggestion", o.getEnrolledCount() >= o.getMinEnrollment()
                    ? "建议开课" : "建议取消");
            item.put("status", o.getStatus());
            return item;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(items);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long offeringId) {
        Offering offering = offeringMapper.selectById(offeringId);
        if (offering == null) throw new BusinessException(404, "开课记录不存在");
        if (!OfferingStatus.PENDING.name().equals(offering.getStatus())) {
            throw new BusinessException(409, "只有待审核的开课记录才能审核");
        }

        offering.setStatus(OfferingStatus.APPROVED.name());
        offering.setUpdatedAt(LocalDateTime.now());
        offeringMapper.updateById(offering);

        LambdaQueryWrapper<Enrollment> eq = Wrappers.lambdaQuery();
        eq.eq(Enrollment::getOfferingId, offeringId)
                .eq(Enrollment::getStatus, EnrollmentStatus.ENROLLED.name());
        Enrollment update = new Enrollment();
        update.setStatus(EnrollmentStatus.APPROVED.name());
        update.setReviewedAt(LocalDateTime.now());
        enrollmentMapper.update(update, eq);

        clearCache(offeringId);

        eventPublisher.publishEvent(new ReviewEvent(offeringId, "APPROVED"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long offeringId) {
        Offering offering = offeringMapper.selectById(offeringId);
        if (offering == null) throw new BusinessException(404, "开课记录不存在");
        if (!OfferingStatus.PENDING.name().equals(offering.getStatus())) {
            throw new BusinessException(409, "只有待审核的开课记录才能审核");
        }

        offering.setStatus(OfferingStatus.REJECTED.name());
        offering.setUpdatedAt(LocalDateTime.now());
        offeringMapper.updateById(offering);

        LambdaQueryWrapper<Enrollment> eq = Wrappers.lambdaQuery();
        eq.eq(Enrollment::getOfferingId, offeringId)
                .eq(Enrollment::getStatus, EnrollmentStatus.ENROLLED.name());
        Enrollment update = new Enrollment();
        update.setStatus(EnrollmentStatus.REJECTED.name());
        update.setReviewedAt(LocalDateTime.now());
        enrollmentMapper.update(update, eq);

        clearCache(offeringId);

        eventPublisher.publishEvent(new ReviewEvent(offeringId, "REJECTED"));
    }

    private void clearCache(Long offeringId) {
        try {
            Set<String> keys = redisTemplate.keys("cas:course:list:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            redisTemplate.delete("cas:offering:" + offeringId);
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache clear in review");
        }
    }
}