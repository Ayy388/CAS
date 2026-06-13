package com.cas.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private String courseName;
    private Integer isRead;
    private LocalDateTime createdAt;
}