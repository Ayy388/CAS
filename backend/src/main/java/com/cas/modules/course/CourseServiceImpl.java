package com.cas.modules.course;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.common.exception.BusinessException;
import com.cas.modules.course.dto.CourseRequest;
import com.cas.modules.course.dto.CourseVO;
import com.cas.modules.course.entity.Course;
import com.cas.modules.course.mapper.CourseMapper;
import com.cas.modules.offering.mapper.OfferingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final OfferingMapper offeringMapper;

    @Override
    public Page<CourseVO> listCourses(int page, int pageSize, String keyword, String type) {
        Page<Course> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Course> query = Wrappers.lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            query.and(w -> w.like(Course::getName, keyword)
                    .or().like(Course::getCode, keyword));
        }
        if (StringUtils.hasText(type)) {
            query.eq(Course::getType, type);
        }
        query.orderByDesc(Course::getCreatedAt);
        Page<Course> result = courseMapper.selectPage(mpPage, query);

        Page<CourseVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseVO createCourse(CourseRequest request) {
        // Check unique code
        LambdaQueryWrapper<Course> check = Wrappers.lambdaQuery();
        check.eq(Course::getCode, request.getCode());
        if (courseMapper.selectCount(check) > 0) {
            throw new BusinessException(409, "课程编号已存在");
        }

        Course course = new Course();
        course.setCode(request.getCode());
        course.setName(request.getName());
        course.setType(request.getType());
        course.setCredits(request.getCredits());
        course.setHours(request.getHours());
        course.setDescription(request.getDescription());
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        courseMapper.insert(course);
        return toVO(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseVO updateCourse(Long id, CourseRequest request) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // Check unique code if changed
        if (!course.getCode().equals(request.getCode())) {
            LambdaQueryWrapper<Course> check = Wrappers.lambdaQuery();
            check.eq(Course::getCode, request.getCode());
            if (courseMapper.selectCount(check) > 0) {
                throw new BusinessException(409, "课程编号已存在");
            }
        }

        course.setCode(request.getCode());
        course.setName(request.getName());
        course.setType(request.getType());
        course.setCredits(request.getCredits());
        course.setHours(request.getHours());
        course.setDescription(request.getDescription());
        course.setUpdatedAt(LocalDateTime.now());
        courseMapper.updateById(course);
        return toVO(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // Check if offerings reference this course
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.cas.modules.offering.entity.Offering> offeringQuery = Wrappers.lambdaQuery();
        offeringQuery.eq(com.cas.modules.offering.entity.Offering::getCourseId, id);
        if (offeringMapper.selectCount(offeringQuery) > 0) {
            throw new BusinessException(409, "该课程下存在开课记录，无法删除");
        }

        courseMapper.deleteById(id);
    }

    private CourseVO toVO(Course course) {
        CourseVO vo = new CourseVO();
        vo.setId(course.getId());
        vo.setCode(course.getCode());
        vo.setName(course.getName());
        vo.setType(course.getType());
        vo.setCredits(course.getCredits());
        vo.setHours(course.getHours());
        vo.setDescription(course.getDescription());
        vo.setCreatedAt(course.getCreatedAt());
        return vo;
    }
}