package com.cas.modules.offering.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_offering")
public class Offering {
    private Long id;
    private Long semesterId;
    private Long courseId;
    private Long teacherId;
    private Integer maxCapacity;
    private Integer minEnrollment;
    private Integer enrolledCount;
    private String openGrade;
    private String openMajor;
    private String location;
    private String schedule;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}