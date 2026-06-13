package com.cas.modules.course.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cas.common.exception.BusinessException;
import com.cas.common.result.ApiResponse;
import com.cas.modules.course.entity.Course;
import com.cas.modules.course.mapper.CourseMapper;
import com.cas.auth.entity.User;
import com.cas.auth.mapper.UserMapper;
import com.cas.modules.enrollment.entity.Enrollment;
import com.cas.modules.enrollment.mapper.EnrollmentMapper;
import com.cas.modules.offering.entity.Offering;
import com.cas.modules.offering.mapper.OfferingMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final OfferingMapper offeringMapper;
    private final CourseMapper courseMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final UserMapper userMapper;

    @GetMapping("/courses")
    public ApiResponse<List<Map<String, Object>>> myCourses(Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();

        LambdaQueryWrapper<Offering> q = Wrappers.lambdaQuery();
        q.eq(Offering::getTeacherId, teacherId);
        List<Offering> offerings = offeringMapper.selectList(q);

        return ApiResponse.success(offerings.stream().map(o -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("courseId", o.getCourseId());
            Course c = courseMapper.selectById(o.getCourseId());
            m.put("courseName", c != null ? c.getName() : "");
            m.put("maxCapacity", o.getMaxCapacity());
            m.put("enrolledCount", o.getEnrolledCount());
            m.put("status", o.getStatus());
            return m;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/courses/{id}/students")
    public ApiResponse<List<Map<String, Object>>> students(@PathVariable Long id, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        Offering offering = offeringMapper.selectById(id);
        if (offering == null || !offering.getTeacherId().equals(teacherId)) {
            throw new BusinessException(403, "无权查看此课程的学生名单");
        }

        LambdaQueryWrapper<Enrollment> q = Wrappers.lambdaQuery();
        q.eq(Enrollment::getOfferingId, id);
        List<Enrollment> enrollments = enrollmentMapper.selectList(q);

        return ApiResponse.success(enrollments.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("enrollmentId", e.getId());
            m.put("studentId", e.getStudentId());
            User student = userMapper.selectById(e.getStudentId());
            m.put("studentName", student != null ? student.getRealName() : "");
            m.put("studentNo", student != null ? student.getStudentId() : "");
            m.put("enrolledAt", e.getEnrolledAt());
            return m;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/courses/{id}/students/export")
    public ResponseEntity<byte[]> exportStudents(@PathVariable Long id, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        Offering offering = offeringMapper.selectById(id);
        if (offering == null || !offering.getTeacherId().equals(teacherId)) {
            throw new BusinessException(403, "无权导出此课程的学生名单");
        }

        LambdaQueryWrapper<Enrollment> q = Wrappers.lambdaQuery();
        q.eq(Enrollment::getOfferingId, id);
        List<Enrollment> enrollments = enrollmentMapper.selectList(q);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("学生名单");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("学号");
            header.createCell(1).setCellValue("姓名");
            header.createCell(2).setCellValue("报名时间");

            int rowIdx = 1;
            for (Enrollment e : enrollments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getStudentId() != null ? e.getStudentId().toString() : "");
                row.createCell(1).setCellValue(e.getStudentId() != null ? userMapper.selectById(e.getStudentId()).getRealName() : "");
                row.createCell(2).setCellValue(e.getEnrolledAt() != null ? e.getEnrolledAt().toString() : "");
            }

            wb.write(out);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "students.xlsx");
            return ResponseEntity.ok().headers(headers).body(out.toByteArray());
        } catch (Exception ex) {
            throw new BusinessException(500, "导出失败");
        }
    }
}