package com.cas;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cas.auth.entity.User;
import com.cas.auth.mapper.UserMapper;
import com.cas.modules.notification.entity.Notification;
import com.cas.modules.notification.mapper.NotificationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Phase10RemainingTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationMapper notificationMapper;

    private String adminToken() {
        var req = new com.cas.auth.LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/auth/login", req, Map.class);
        Map data = (Map) resp.getBody().get("data");
        return (String) data.get("token");
    }

    private String tokenFor(String username) {
        var req = new com.cas.auth.LoginRequest();
        req.setUsername(username);
        req.setPassword("123456");
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/auth/login", req, Map.class);
        if (resp.getBody().get("data") == null) return null;
        Map data = (Map) resp.getBody().get("data");
        return (String) data.get("token");
    }

    private HttpEntity<Object> bearerPost(String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        if (body != null) headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Object> adminPost(Object body) {
        return bearerPost(adminToken(), body);
    }

    private void ensureUsers() {
        if (userMapper.selectOne(Wrappers.lambdaQuery(new User()).eq(User::getUsername, "tch_rem")) == null) {
            User t = new User();
            t.setUsername("tch_rem");
            t.setPassword(passwordEncoder.encode("123456"));
            t.setRealName("剩余测试教师");
            t.setRole("TEACHER");
            t.setStatus(1);
            t.setDepartment("计算机学院");
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(t);
        }
        if (userMapper.selectOne(Wrappers.lambdaQuery(new User()).eq(User::getUsername, "stu_rem")) == null) {
            User s = new User();
            s.setUsername("stu_rem");
            s.setPassword(passwordEncoder.encode("123456"));
            s.setRealName("剩余测试学生");
            s.setRole("STUDENT");
            s.setStatus(1);
            s.setStudentId("20243001");
            s.setDepartment("计算机学院");
            s.setMajor("计算机科学与技术");
            s.setGrade("2024");
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(s);
        }
    }

    private Long getUserId(String username) {
        return userMapper.selectOne(Wrappers.lambdaQuery(new User()).eq(User::getUsername, username)).getId();
    }

    // ==================== Notification Tests ====================

    @Test
    void should_list_notifications() {
        ensureUsers();
        Long studentId = getUserId("stu_rem");
        String studentToken = tokenFor("stu_rem");

        // Create a notification for the student
        Notification n = new Notification();
        n.setUserId(studentId);
        n.setType("SYSTEM");
        n.setTitle("系统通知标题");
        n.setContent("系统通知内容");
        n.setCourseName("测试课程");
        n.setIsRead(0);
        n.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(n);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/notifications",
                HttpMethod.GET, bearerPost(studentToken, null), Map.class);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(200, response.getBody().get("code"));
    }

    @Test
    void should_mark_notification_read() {
        ensureUsers();
        Long studentId = getUserId("stu_rem");
        String studentToken = tokenFor("stu_rem");

        Notification n = new Notification();
        n.setUserId(studentId);
        n.setType("SYSTEM");
        n.setTitle("已读测试");
        n.setContent("已读测试内容");
        n.setIsRead(0);
        n.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(n);
        Long notifId = n.getId();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/notifications/" + notifId + "/read",
                HttpMethod.PUT, bearerPost(studentToken, null), Map.class);

        assertEquals(200, response.getStatusCode().value());

        // Verify isRead = 1
        Notification updated = notificationMapper.selectById(notifId);
        assertEquals(1, updated.getIsRead());
    }

    // ==================== Teacher Tests ====================

    @Test
    void should_list_teacher_courses() {
        ensureUsers();
        String teacherToken = tokenFor("tch_rem");
        assertNotNull(teacherToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/teacher/courses",
                HttpMethod.GET, bearerPost(teacherToken, null), Map.class);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void should_get_dashboard_stats() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/dashboard/stats",
                HttpMethod.GET, adminPost(null), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map data = (Map) response.getBody().get("data");
        assertNotNull(data.get("totalCourses"));
        assertNotNull(data.get("totalTeachers"));
        assertNotNull(data.get("totalStudents"));
        assertNotNull(data.get("totalEnrollments"));
    }

    @Test
    void should_get_top_courses() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/dashboard/top-courses",
                HttpMethod.GET, adminPost(null), Map.class);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void should_get_trend() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/dashboard/trend",
                HttpMethod.GET, adminPost(null), Map.class);

        assertEquals(200, response.getStatusCode().value());
    }
}