package com.cas.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    private Long id;
    private String username;
    private String password;
    private String realName;
    private String role;
    private String studentId;
    private String department;
    private String major;
    private String grade;
    private String email;
    private String phone;
    private String avatarUrl;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}