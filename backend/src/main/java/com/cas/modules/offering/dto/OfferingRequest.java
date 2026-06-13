package com.cas.modules.offering.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OfferingRequest {

    @NotNull(message = "学期不能为空")
    private Long semesterId;

    @NotNull(message = "课程不能为空")
    private Long courseId;

    @NotNull(message = "教师不能为空")
    private Long teacherId;

    @Min(value = 1, message = "最大容量不能少于1")
    private Integer maxCapacity;

    @Min(value = 1, message = "最低开课人数不能少于1")
    private Integer minEnrollment;

    private String openGrade;
    private String openMajor;
    private String location;
    private String schedule;
}