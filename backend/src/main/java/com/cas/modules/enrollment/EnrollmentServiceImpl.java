package com.cas.modules.enrollment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cas.auth.entity.User;
import com.cas.auth.mapper.UserMapper;
import com.cas.common.enums.CampaignStatus;
import com.cas.common.enums.EnrollmentStatus;
import com.cas.common.exception.BusinessException;
import com.cas.modules.campaign.entity.Campaign;
import com.cas.modules.campaign.mapper.CampaignMapper;
import com.cas.modules.course.entity.Course;
import com.cas.modules.course.mapper.CourseMapper;
import com.cas.modules.enrollment.dto.EnrollmentVO;
import com.cas.modules.enrollment.entity.Enrollment;
import com.cas.modules.enrollment.mapper.EnrollmentMapper;
import com.cas.modules.offering.entity.Offering;
import com.cas.modules.offering.mapper.OfferingMapper;
import com.cas.mq.event.EnrollEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentMapper enrollmentMapper;
    private final OfferingMapper offeringMapper;
    private final CampaignMapper campaignMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EnrollmentVO enroll(Long offeringId, Long studentId) {
        // ① Redis request dedup (fault-tolerant)
        String dedupKey = "cas:enroll:request:" + studentId;
        try {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", 2, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(acquired)) {
                throw new BusinessException(429, "操作太频繁，请稍后再试");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping dedup check");
        }

        // ② Current active campaign
        LambdaQueryWrapper<Campaign> cq = Wrappers.lambdaQuery();
        cq.eq(Campaign::getStatus, CampaignStatus.ACTIVE.name())
                .le(Campaign::getStartTime, LocalDateTime.now())
                .ge(Campaign::getEndTime, LocalDateTime.now())
                .last("LIMIT 1");
        Campaign campaign = campaignMapper.selectOne(cq);
        if (campaign == null) {
            throw new BusinessException(409, "当前没有进行中的选课活动");
        }

        // ③ BR-03: duplicate check
        LambdaQueryWrapper<Enrollment> dup = Wrappers.lambdaQuery();
        dup.eq(Enrollment::getCampaignId, campaign.getId())
                .eq(Enrollment::getStudentId, studentId)
                .in(Enrollment::getStatus, EnrollmentStatus.ENROLLED.name(), EnrollmentStatus.APPROVED.name());
        if (enrollmentMapper.selectCount(dup) > 0) {
            throw new BusinessException(409, "您已选课，不可重复选课");
        }

        // Get offering
        Offering offering = offeringMapper.selectById(offeringId);
        if (offering == null) {
            throw new BusinessException(404, "开课记录不存在");
        }

        // ④ BR-05: open range check
        User student = userMapper.selectById(studentId);
        if (offering.getOpenGrade() != null && student != null
                && !offering.getOpenGrade().equals(student.getGrade())) {
            throw new BusinessException(409, "您不符合开放范围");
        }
        if (offering.getOpenMajor() != null && student != null
                && !offering.getOpenMajor().equals(student.getMajor())) {
            throw new BusinessException(409, "您不符合开放范围");
        }

        // ⑤ Redis capacity pre-check (fault-tolerant)
        String countKey = "cas:enrollment:count:" + offeringId;
        try {
            Integer cachedCount = (Integer) redisTemplate.opsForValue().get(countKey);
            if (cachedCount != null && cachedCount >= offering.getMaxCapacity()) {
                throw new BusinessException(409, "已满员");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping capacity pre-check");
        }

        // ⑥ Redis INCR temp count (fault-tolerant)
        String tempKey = "cas:enrollment:temp:" + offeringId;
        boolean redisIncrDone = false;
        try {
            Long tempCount = redisTemplate.opsForValue().increment(tempKey);
            redisIncrDone = true;
            if (tempCount != null && tempCount > offering.getMaxCapacity()) {
                redisTemplate.opsForValue().decrement(tempKey);
                throw new BusinessException(409, "已满员");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping temp count");
        }

        try {
            // ⑦ MySQL atomic UPDATE
            int updated = offeringMapper.updateEnrolledCount(offeringId, offering.getMaxCapacity());
            if (updated == 0) {
                throw new BusinessException(409, "已满员");
            }

            offering = offeringMapper.selectById(offeringId);

            // ⑧ INSERT enrollment
            Enrollment enrollment = new Enrollment();
            enrollment.setCampaignId(campaign.getId());
            enrollment.setOfferingId(offeringId);
            enrollment.setStudentId(studentId);
            enrollment.setStatus(EnrollmentStatus.ENROLLED.name());
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setCreatedAt(LocalDateTime.now());
            enrollment.setUpdatedAt(LocalDateTime.now());
            enrollmentMapper.insert(enrollment);

            // ⑨ Clear cache (fault-tolerant)
            try {
                if (redisIncrDone) {
                    redisTemplate.opsForValue().decrement(tempKey);
                }
                Set<String> keys = redisTemplate.keys("cas:course:list:*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
                redisTemplate.delete("cas:offering:" + offeringId);
                redisTemplate.opsForValue().set(countKey, offering.getEnrolledCount());
            } catch (Exception e) {
                log.warn("Redis unavailable, skipping cache cleanup after enroll");
            }

            Course course = courseMapper.selectById(offering.getCourseId());
            eventPublisher.publishEvent(new EnrollEvent(
                    enrollment.getId(), studentId, offeringId, campaign.getId(),
                    course != null ? course.getName() : ""));

            return toVO(enrollment, offering);
        } catch (BusinessException e) {
            if (redisIncrDone) {
                try { redisTemplate.opsForValue().decrement(tempKey); } catch (Exception ignored) {}
            }
            throw e;
        } catch (Exception e) {
            if (redisIncrDone) {
                try { redisTemplate.opsForValue().decrement(tempKey); } catch (Exception ignored) {}
            }
            log.error("Enrollment failed", e);
            throw new BusinessException(500, "选课失败，请重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dropEnrollment(Long enrollmentId, Long studentId) {
        Enrollment enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null) throw new BusinessException(404, "选课记录不存在");
        if (!enrollment.getStudentId().equals(studentId)) throw new BusinessException(403, "无权操作此选课记录");

        Campaign campaign = campaignMapper.selectById(enrollment.getCampaignId());
        if (campaign != null && CampaignStatus.ENDED.name().equals(campaign.getStatus())) {
            throw new BusinessException(409, "已超过退课时间");
        }

        int updated = offeringMapper.decrementEnrolledCount(enrollment.getOfferingId());
        if (updated == 0) throw new BusinessException(500, "退课失败");

        enrollmentMapper.deleteById(enrollmentId);
        try {
            Set<String> keys = redisTemplate.keys("cas:course:list:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            redisTemplate.delete("cas:offering:" + enrollment.getOfferingId());
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache cleanup after drop");
        }
    }

    @Override
    public List<EnrollmentVO> listMyEnrollments(Long studentId) {
        LambdaQueryWrapper<Enrollment> q = Wrappers.lambdaQuery();
        q.eq(Enrollment::getStudentId, studentId).orderByDesc(Enrollment::getEnrolledAt);
        return enrollmentMapper.selectList(q).stream()
                .map(e -> toVO(e, offeringMapper.selectById(e.getOfferingId())))
                .collect(Collectors.toList());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEnrollSuccess(EnrollEvent event) {
        // Temp key already decremented in main flow. Reserved for future MQ delivery.
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onEnrollFailed(EnrollEvent event) {
        // Temp key already decremented in catch blocks. Reserved for future MQ delivery.
    }

    private EnrollmentVO toVO(Enrollment e, Offering o) {
        EnrollmentVO vo = new EnrollmentVO();
        vo.setId(e.getId());
        vo.setCampaignId(e.getCampaignId());
        vo.setOfferingId(e.getOfferingId());
        vo.setStudentId(e.getStudentId());
        vo.setStatus(e.getStatus());
        vo.setEnrolledAt(e.getEnrolledAt());
        vo.setReviewedAt(e.getReviewedAt());
        if (o != null) {
            Course c = courseMapper.selectById(o.getCourseId());
            if (c != null) {
                vo.setOfferingName(c.getName());
                vo.setCourseName(c.getName());
                vo.setCredits(c.getCredits());
                vo.setHours(c.getHours());
            }
            if (o.getTeacherId() != null) {
                User teacher = userMapper.selectById(o.getTeacherId());
                if (teacher != null) vo.setTeacherName(teacher.getRealName());
            }
            vo.setLocation(o.getLocation());
            vo.setSchedule(o.getSchedule());
            vo.setOpenGrade(o.getOpenGrade());
            vo.setOpenMajor(o.getOpenMajor());
            vo.setMaxCapacity(o.getMaxCapacity());
            vo.setMinEnrollment(o.getMinEnrollment());
            vo.setEnrolledCount(o.getEnrolledCount());
        }
        return vo;
    }
}