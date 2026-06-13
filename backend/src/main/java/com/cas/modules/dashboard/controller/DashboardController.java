package com.cas.modules.dashboard.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cas.auth.entity.User;
import com.cas.auth.mapper.UserMapper;
import com.cas.common.result.ApiResponse;
import com.cas.modules.course.entity.Course;
import com.cas.modules.course.mapper.CourseMapper;
import com.cas.modules.enrollment.entity.Enrollment;
import com.cas.modules.enrollment.mapper.EnrollmentMapper;
import com.cas.modules.offering.entity.Offering;
import com.cas.modules.offering.mapper.OfferingMapper;
import com.cas.modules.semester.entity.Semester;
import com.cas.modules.semester.mapper.SemesterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final CourseMapper courseMapper;
    private final OfferingMapper offeringMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final SemesterMapper semesterMapper;
    private final UserMapper userMapper;

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();

        long totalCourses = courseMapper.selectCount(Wrappers.emptyWrapper());
        stats.put("totalCourses", totalCourses);

        stats.put("totalTeachers", countByRole("TEACHER"));

        stats.put("totalStudents", countByRole("STUDENT"));

        long totalEnrollments = enrollmentMapper.selectCount(Wrappers.emptyWrapper());
        stats.put("totalEnrollments", totalEnrollments);

        return ApiResponse.success(stats);
    }

    @GetMapping("/top-courses")
    public ApiResponse<List<Map<String, Object>>> topCourses() {
        // Get active semester
        LambdaQueryWrapper<Semester> sq = Wrappers.lambdaQuery();
        sq.eq(Semester::getStatus, "ACTIVE");
        Semester s = semesterMapper.selectOne(sq);
        if (s == null) return ApiResponse.success(Collections.emptyList());

        LambdaQueryWrapper<Offering> oq = Wrappers.lambdaQuery();
        oq.eq(Offering::getSemesterId, s.getId());
        List<Offering> offerings = offeringMapper.selectList(oq);

        List<Map<String, Object>> result = offerings.stream()
                .sorted((a, b) -> Integer.compare(b.getEnrolledCount(), a.getEnrolledCount()))
                .limit(10)
                .map(o -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("offeringId", o.getId());
                    Course c = courseMapper.selectById(o.getCourseId());
                    m.put("courseName", c != null ? c.getName() : "");
                    m.put("enrollmentCount", o.getEnrolledCount());
                    return m;
                }).toList();
        return ApiResponse.success(result);
    }

    @GetMapping("/trend")
    public ApiResponse<List<Map<String, Object>>> trend() {
        // Get active semester
        LambdaQueryWrapper<Semester> sq = Wrappers.lambdaQuery();
        sq.eq(Semester::getStatus, "ACTIVE");
        Semester s = semesterMapper.selectOne(sq);
        if (s == null) return ApiResponse.success(Collections.emptyList());

        // Get all offerings in this semester
        LambdaQueryWrapper<Offering> oq = Wrappers.lambdaQuery();
        oq.eq(Offering::getSemesterId, s.getId());
        List<Offering> offerings = offeringMapper.selectList(oq);
        if (offerings.isEmpty()) return ApiResponse.success(Collections.emptyList());

        List<Long> offeringIds = offerings.stream().map(Offering::getId).collect(Collectors.toList());

        // Group enrollments by date
        LambdaQueryWrapper<Enrollment> eq = Wrappers.lambdaQuery();
        eq.in(Enrollment::getOfferingId, offeringIds)
                .orderByAsc(Enrollment::getEnrolledAt);
        List<Enrollment> enrollments = enrollmentMapper.selectList(eq);

        Map<String, Long> dailyCount = new LinkedHashMap<>();
        for (Enrollment e : enrollments) {
            if (e.getEnrolledAt() != null) {
                String date = e.getEnrolledAt().toLocalDate().toString();
                dailyCount.merge(date, 1L, Long::sum);
            }
        }

        List<Map<String, Object>> result = dailyCount.entrySet().stream().map(entry -> {
            Map<String, Object> m = new HashMap<>();
            m.put("date", entry.getKey());
            m.put("count", entry.getValue());
            return m;
        }).collect(Collectors.toList());

        return ApiResponse.success(result);
    }

    private long countByRole(String role) {
        LambdaQueryWrapper<User> q = Wrappers.lambdaQuery();
        q.eq(User::getRole, role);
        return userMapper.selectCount(q);
    }
}