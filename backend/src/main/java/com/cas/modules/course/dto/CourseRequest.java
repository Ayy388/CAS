package com.cas.modules.course.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseRequest {
    @NotBlank(message = "课程编号不能为空")
    private String code;

    @NotBlank(message = "课程名称不能为空")
    private String name;

    @NotBlank(message = "课程类型不能为空")
    private String type;

    @NotNull(message = "学分不能为空")
    @DecimalMin(value = "0.5", message = "学分不能小于0.5")
    @DecimalMax(value = "99.9", message = "学分不能大于99.9")
    private BigDecimal credits;

    @NotNull(message = "课时不能为空")
    @Min(value = 1, message = "课时不能少于1")
    private Integer hours;

    private String description;
}