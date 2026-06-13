package com.cas.modules.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {
    private Long id;
    private String code;
    private String name;
    private String type;
    private BigDecimal credits;
    private Integer hours;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}