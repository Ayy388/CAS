package com.cas.modules.offering;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.common.exception.BusinessException;
import com.cas.auth.entity.User;
import com.cas.auth.mapper.UserMapper;
import com.cas.modules.course.entity.Course;
import com.cas.modules.course.mapper.CourseMapper;
import com.cas.modules.offering.dto.OfferingRequest;
import com.cas.modules.offering.dto.OfferingVO;
import com.cas.modules.offering.entity.Offering;
import com.cas.modules.offering.mapper.OfferingMapper;
import com.cas.modules.semester.entity.Semester;
import com.cas.modules.semester.mapper.SemesterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferingServiceImpl implements OfferingService {

    private final OfferingMapper offeringMapper;
    private final CourseMapper courseMapper;
    private final SemesterMapper semesterMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Page<OfferingVO> listOfferings(int page, int pageSize) {
        Page<Offering> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Offering> query = Wrappers.lambdaQuery();
        query.orderByDesc(Offering::getCreatedAt);
        Page<Offering> result = offeringMapper.selectPage(mpPage, query);
        return toVOPage(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OfferingVO createOffering(OfferingRequest request) {
        if (courseMapper.selectById(request.getCourseId()) == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (semesterMapper.selectById(request.getSemesterId()) == null) {
            throw new BusinessException(404, "学期不存在");
        }

        Offering offering = new Offering();
        offering.setSemesterId(request.getSemesterId());
        offering.setCourseId(request.getCourseId());
        offering.setTeacherId(request.getTeacherId());
        offering.setMaxCapacity(request.getMaxCapacity());
        offering.setMinEnrollment(request.getMinEnrollment());
        offering.setEnrolledCount(0);
        offering.setOpenGrade(request.getOpenGrade());
        offering.setOpenMajor(request.getOpenMajor());
        offering.setLocation(request.getLocation());
        offering.setSchedule(request.getSchedule());
        offering.setStatus("PENDING");
        offering.setCreatedAt(LocalDateTime.now());
        offering.setUpdatedAt(LocalDateTime.now());
        offeringMapper.insert(offering);
        clearCourseListCache();
        return toVO(offering);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OfferingVO updateOffering(Long id, OfferingRequest request) {
        Offering offering = offeringMapper.selectById(id);
        if (offering == null) {
            throw new BusinessException(404, "开课记录不存在");
        }
        offering.setSemesterId(request.getSemesterId());
        offering.setCourseId(request.getCourseId());
        offering.setTeacherId(request.getTeacherId());
        offering.setMaxCapacity(request.getMaxCapacity());
        offering.setMinEnrollment(request.getMinEnrollment());
        offering.setOpenGrade(request.getOpenGrade());
        offering.setOpenMajor(request.getOpenMajor());
        offering.setLocation(request.getLocation());
        offering.setSchedule(request.getSchedule());
        offering.setUpdatedAt(LocalDateTime.now());
        offeringMapper.updateById(offering);
        clearCourseListCache();
        deleteCache("cas:offering:" + id);
        return toVO(offering);
    }

    @Override
    public Page<OfferingVO> listStudentOfferings(int page, int pageSize, String keyword, String type) {
        String cacheKey = "cas:course:list:" + (type != null ? type : "all") + ":" + page;
        // Try cache read
        try {
            @SuppressWarnings("unchecked")
            Page<OfferingVO> cached = (Page<OfferingVO>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache read for listStudentOfferings");
        }

        LambdaQueryWrapper<Semester> semQuery = Wrappers.lambdaQuery();
        semQuery.eq(Semester::getStatus, "ACTIVE");
        Semester activeSemester = semesterMapper.selectOne(semQuery);
        if (activeSemester == null) {
            return new Page<>(page, pageSize, 0);
        }

        Page<Offering> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Offering> query = Wrappers.lambdaQuery();
        query.eq(Offering::getSemesterId, activeSemester.getId())
                .in(Offering::getStatus, "PENDING", "APPROVED", "REJECTED");

        if (StringUtils.hasText(type)) {
            LambdaQueryWrapper<Course> courseQuery = Wrappers.lambdaQuery();
            courseQuery.eq(Course::getType, type);
            List<Course> courses = courseMapper.selectList(courseQuery);
            if (!courses.isEmpty()) {
                query.in(Offering::getCourseId, courses.stream().map(Course::getId).collect(Collectors.toList()));
            }
        }

        if (StringUtils.hasText(keyword)) {
            LambdaQueryWrapper<Course> kwQuery = Wrappers.lambdaQuery();
            kwQuery.like(Course::getName, keyword);
            List<Course> matchedCourses = courseMapper.selectList(kwQuery);
            if (!matchedCourses.isEmpty()) {
                query.in(Offering::getCourseId, matchedCourses.stream().map(Course::getId).collect(Collectors.toList()));
            } else {
                return new Page<>(page, pageSize, 0);
            }
        }

        query.orderByDesc(Offering::getCreatedAt);
        Page<Offering> result = offeringMapper.selectPage(mpPage, query);
        Page<OfferingVO> voPage = toVOPage(result);

        try {
            redisTemplate.opsForValue().set(cacheKey, voPage, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache write for listStudentOfferings");
        }
        return voPage;
    }

    @Override
    public OfferingVO getStudentOfferingDetail(Long id) {
        String cacheKey = "cas:offering:" + id;
        try {
            OfferingVO cached = (OfferingVO) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache read for getStudentOfferingDetail");
        }

        Offering offering = offeringMapper.selectById(id);
        if (offering == null) {
            throw new BusinessException(404, "课程不存在");
        }

        OfferingVO vo = toVO(offering);
        try {
            redisTemplate.opsForValue().set(cacheKey, vo, 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache write for getStudentOfferingDetail");
        }
        return vo;
    }

    private void clearCourseListCache() {
        try {
            redisTemplate.delete(redisTemplate.keys("cas:course:list:*"));
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache clear");
        }
    }

    private void deleteCache(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache delete");
        }
    }

    private Page<OfferingVO> toVOPage(Page<Offering> page) {
        Page<OfferingVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    private OfferingVO toVO(Offering offering) {
        OfferingVO vo = new OfferingVO();
        vo.setId(offering.getId());
        vo.setSemesterId(offering.getSemesterId());
        vo.setCourseId(offering.getCourseId());
        vo.setTeacherId(offering.getTeacherId());
        vo.setMaxCapacity(offering.getMaxCapacity());
        vo.setMinEnrollment(offering.getMinEnrollment());
        vo.setEnrolledCount(offering.getEnrolledCount());
        vo.setSeatsRemaining(offering.getMaxCapacity() - offering.getEnrolledCount());
        vo.setOpenGrade(offering.getOpenGrade());
        vo.setOpenMajor(offering.getOpenMajor());
        vo.setLocation(offering.getLocation());
        vo.setSchedule(offering.getSchedule());
        vo.setStatus(offering.getStatus());

        Course course = courseMapper.selectById(offering.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
            vo.setCourseType(course.getType());
        }

        User teacher = userMapper.selectById(offering.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
        }

        Semester semester = semesterMapper.selectById(offering.getSemesterId());
        if (semester != null) {
            vo.setSemesterName(semester.getName());
        }

        return vo;
    }
}