package com.cas.modules.enrollment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("enrollment")
public class Enrollment {
    private Long id;
    private Long campaignId;
    private Long offeringId;
    private Long studentId;
    private String status;
    private LocalDateTime enrolledAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}