-- ============================================
-- CAS V1.0 数据库初始化脚本
-- 目标：MySQL 8.0+
-- 字符集：utf8mb4
-- ============================================

DROP DATABASE IF EXISTS cas;
CREATE DATABASE IF NOT EXISTS cas
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cas;

-- -------------------------------------------
-- 1. 用户表
-- -------------------------------------------
CREATE TABLE sys_user (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL,
    password     VARCHAR(255) NOT NULL COMMENT 'BCrypt encrypted',
    real_name    VARCHAR(50)  NOT NULL,
    role         VARCHAR(20)  NOT NULL COMMENT 'STUDENT / TEACHER / ADMIN',
    student_id   VARCHAR(20)  NULL     COMMENT 'student number, only for STUDENT role',
    department   VARCHAR(100) NULL     COMMENT 'college or department',
    major        VARCHAR(100) NULL     COMMENT 'major',
    grade        VARCHAR(20)  NULL     COMMENT 'grade, e.g. 2023级',
    email        VARCHAR(100) NULL,
    phone        VARCHAR(20)  NULL,
    avatar_url   VARCHAR(255) NULL,
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=enabled 0=disabled',
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NOT NULL,

    CONSTRAINT uq_sys_user_username   UNIQUE (username),
    CONSTRAINT uq_sys_user_student_id UNIQUE (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sys_user_role ON sys_user(role);

-- -------------------------------------------
-- 2. 学期表
-- -------------------------------------------
CREATE TABLE semester (
    id             BIGINT      AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL COMMENT 'e.g. 2025-2026第一学期',
    academic_year  VARCHAR(20)  NOT NULL COMMENT 'e.g. 2025-2026',
    semester_type  VARCHAR(10)  NOT NULL COMMENT 'FIRST / SECOND',
    start_date     DATE         NOT NULL,
    end_date       DATE         NOT NULL,
    status         VARCHAR(10)  NOT NULL DEFAULT 'INACTIVE' COMMENT 'ACTIVE / INACTIVE',
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_semester_status ON semester(status);

-- -------------------------------------------
-- 3. 课程基础信息表
-- -------------------------------------------
CREATE TABLE course (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL COMMENT 'course code, e.g. GE001',
    name        VARCHAR(100) NOT NULL COMMENT 'course name',
    type        VARCHAR(20)  NOT NULL COMMENT 'REQUIRED / ELECTIVE_MAJOR / ELECTIVE_GENERAL',
    credits     DECIMAL(3,1) NOT NULL COMMENT 'credits',
    hours       INT          NOT NULL COMMENT 'total hours',
    description TEXT         NULL     COMMENT 'course description',
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,

    CONSTRAINT uq_course_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_course_type ON course(type);

-- -------------------------------------------
-- 4. 开课记录表
-- -------------------------------------------
CREATE TABLE course_offering (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    semester_id     BIGINT       NOT NULL,
    course_id       BIGINT       NOT NULL,
    teacher_id      BIGINT       NOT NULL,
    max_capacity    INT          NOT NULL COMMENT 'max students, e.g. 50',
    min_enrollment  INT          NOT NULL COMMENT 'min students to proceed, e.g. 20',
    enrolled_count  INT          NOT NULL DEFAULT 0 COMMENT 'current enrollment count (denormalized)',
    open_grade      VARCHAR(50)  NULL     COMMENT 'open grade, NULL=all grades',
    open_major      VARCHAR(100) NULL     COMMENT 'open major, NULL=all majors',
    location        VARCHAR(100) NULL     COMMENT 'classroom or location',
    schedule        VARCHAR(100) NULL     COMMENT 'schedule, e.g. 周三7-8节',
    status          VARCHAR(15)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / APPROVED / REJECTED',
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,

    CONSTRAINT fk_offering_semester FOREIGN KEY (semester_id) REFERENCES semester(id),
    CONSTRAINT fk_offering_course   FOREIGN KEY (course_id)   REFERENCES course(id),
    CONSTRAINT fk_offering_teacher  FOREIGN KEY (teacher_id)  REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_offering_semester        ON course_offering(semester_id);
CREATE INDEX idx_offering_teacher         ON course_offering(teacher_id);
CREATE INDEX idx_offering_status          ON course_offering(status);
CREATE INDEX idx_offering_semester_status ON course_offering(semester_id, status);

-- -------------------------------------------
-- 5. 选课活动表
-- -------------------------------------------
CREATE TABLE selection_campaign (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT 'campaign name',
    semester_id BIGINT       NOT NULL,
    start_time  DATETIME     NOT NULL COMMENT 'start time (精确到分钟)',
    end_time    DATETIME     NOT NULL COMMENT 'end time',
    status      VARCHAR(10)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / ACTIVE / ENDED',
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,

    CONSTRAINT fk_campaign_semester FOREIGN KEY (semester_id) REFERENCES semester(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_campaign_semester    ON selection_campaign(semester_id);
CREATE INDEX idx_campaign_status      ON selection_campaign(status);
CREATE INDEX idx_campaign_time_range  ON selection_campaign(start_time, end_time);

-- -------------------------------------------
-- 6. 选课记录表
-- -------------------------------------------
CREATE TABLE enrollment (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT      NOT NULL,
    offering_id BIGINT      NOT NULL,
    student_id  BIGINT      NOT NULL,
    status      VARCHAR(10) NOT NULL DEFAULT 'ENROLLED' COMMENT 'ENROLLED / APPROVED / REJECTED / DROPPED',
    enrolled_at DATETIME    NOT NULL COMMENT 'enrollment timestamp',
    reviewed_at DATETIME    NULL     COMMENT 'review timestamp',
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME    NOT NULL,

    CONSTRAINT fk_enrollment_campaign FOREIGN KEY (campaign_id)  REFERENCES selection_campaign(id),
    CONSTRAINT fk_enrollment_offering FOREIGN KEY (offering_id)  REFERENCES course_offering(id),
    CONSTRAINT fk_enrollment_student  FOREIGN KEY (student_id)   REFERENCES sys_user(id),

    CONSTRAINT uq_enrollment_campaign_student UNIQUE (campaign_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_enrollment_offering    ON enrollment(offering_id);
CREATE INDEX idx_enrollment_student     ON enrollment(student_id);
CREATE INDEX idx_enrollment_enrolled_at ON enrollment(enrolled_at);
CREATE INDEX idx_enrollment_status      ON enrollment(status);

-- -------------------------------------------
-- 7. 通知表
-- -------------------------------------------
CREATE TABLE notification (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    type        VARCHAR(10)  NOT NULL COMMENT 'APPROVED / REJECTED / SYSTEM',
    title       VARCHAR(200) NOT NULL COMMENT 'notification title',
    content     TEXT         NULL     COMMENT 'notification content',
    course_name VARCHAR(100) NULL     COMMENT 'related course name',
    is_read     TINYINT      NOT NULL DEFAULT 0 COMMENT '0=unread 1=read',
    created_at  DATETIME     NOT NULL,

    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notification_user_read   ON notification(user_id, is_read);
CREATE INDEX idx_notification_created_at  ON notification(created_at);

-- -------------------------------------------
-- 8. 初始数据：默认管理员账号
-- password: admin123 (BCrypt encrypted)
-- -------------------------------------------
INSERT INTO sys_user (username, password, real_name, role, status, created_at, updated_at)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 'ADMIN', 1, NOW(), NOW());