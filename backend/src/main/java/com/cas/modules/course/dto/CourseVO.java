package com.cas.modules.course.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseVO {
    private Long id;
    private String code;
    private String name;
    private String type;
    private BigDecimal credits;
    private Integer hours;
    private String description;
    private LocalDateTime createdAt;
}