package com.cas.modules.offering.dto;

import lombok.Data;

@Data
public class OfferingVO {
    private Long id;
    private Long semesterId;
    private String semesterName;
    private Long courseId;
    private String courseName;
    private String courseType;
    private Long teacherId;
    private String teacherName;
    private Integer maxCapacity;
    private Integer minEnrollment;
    private Integer enrolledCount;
    private Integer seatsRemaining;
    private String openGrade;
    private String openMajor;
    private String location;
    private String schedule;
    private String status;
}