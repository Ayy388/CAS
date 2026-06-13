-- -------------------------------------------
-- V2__seed_admin.sql
-- 初始数据：默认管理员账号
-- password: admin123 (BCrypt encrypted)
-- -------------------------------------------
INSERT IGNORE INTO sys_user (username, password, real_name, role, status, created_at, updated_at)
VALUES ('admin', '$2a$10$rFM116gu1rpNKmDODWIOsOoNbN1ihao.5eLRT/e90pHMi0HrlCG2e',
        '系统管理员', 'ADMIN', 1, NOW(), NOW());