package com.cas.modules.course;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.modules.course.dto.CourseRequest;
import com.cas.modules.course.dto.CourseVO;

public interface CourseService {
    Page<CourseVO> listCourses(int page, int pageSize, String keyword, String type);
    CourseVO createCourse(CourseRequest request);
    CourseVO updateCourse(Long id, CourseRequest request);
    void deleteCourse(Long id);
}