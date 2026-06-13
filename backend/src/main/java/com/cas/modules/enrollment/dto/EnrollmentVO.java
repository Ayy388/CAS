package com.cas.modules.enrollment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnrollmentVO {
    private Long id;
    private Long campaignId;
    private Long offeringId;
    private String offeringName;
    private String courseName;
    private Long studentId;
    private String studentName;
    private String status;
    private LocalDateTime enrolledAt;
    private LocalDateTime reviewedAt;
    // API文档 §3.5 额外字段（JOIN course_offering + course + sys_user）
    private String teacherName;
    private BigDecimal credits;
    private Integer hours;
    private String location;
    private String schedule;
    private String openGrade;
    private String openMajor;
    private Integer maxCapacity;
    private Integer minEnrollment;
    private Integer enrolledCount;
}