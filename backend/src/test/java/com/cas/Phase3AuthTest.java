package com.cas;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cas.auth.entity.User;
import com.cas.auth.mapper.UserMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Phase3AuthTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeAll
    void setUp() {
        ensureTestUser("student1", "STUDENT", "张三", "2023001", "计算机学院", "软件工程", "2023级");
        ensureTestUser("teacher1", "TEACHER", "李教授", null, "计算机学院", null, null);
    }

    private void ensureTestUser(String username, String role, String realName,
                                 String studentId, String department, String major, String grade) {
        LambdaQueryWrapper<User> query = Wrappers.lambdaQuery();
        query.eq(User::getUsername, username);
        if (userMapper.selectOne(query) == null) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRealName(realName);
            user.setRole(role);
            user.setStudentId(studentId);
            user.setDepartment(department);
            user.setMajor(major);
            user.setGrade(grade);
            user.setStatus(1);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
        }
    }

    @SuppressWarnings("unchecked")
    private String loginAndGetToken(String username, String password) {
        var request = new com.cas.auth.LoginRequest();
        request.setUsername(username);
        request.setPassword(password);

        ResponseEntity<Map> resp = restTemplate.postForEntity(
                "/api/v1/auth/login", request, Map.class);
        Map<String, Object> data = (Map<String, Object>) resp.getBody().get("data");
        return (String) data.get("token");
    }

    // ==================== Login Tests ====================

    @Test
    void should_return_200_with_token_when_login_success() {
        var request = new com.cas.auth.LoginRequest();
        request.setUsername("student1");
        request.setPassword("123456");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login", request, Map.class);

        Map body = response.getBody();
        assertEquals(200, body.get("code"));
        assertEquals("success", body.get("message"));

        Map data = (Map) body.get("data");
        assertNotNull(data.get("token"));
        assertNotNull(data.get("user"));
    }

    @Test
    void should_return_401_when_wrong_password() {
        var request = new com.cas.auth.LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpass");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login", request, Map.class);

        Map body = response.getBody();
        assertEquals(401, body.get("code"));
        assertEquals("用户名或密码错误", body.get("message"));
    }

    @Test
    void should_return_401_when_user_not_found() {
        var request = new com.cas.auth.LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("123456");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login", request, Map.class);

        Map body = response.getBody();
        assertEquals(401, body.get("code"));
    }

    @Test
    void should_return_409_when_user_disabled() {
        User disabledUser = new User();
        disabledUser.setUsername("disableduser");
        disabledUser.setPassword(passwordEncoder.encode("123456"));
        disabledUser.setRealName("已禁用");
        disabledUser.setRole("STUDENT");
        disabledUser.setStatus(0);
        disabledUser.setCreatedAt(LocalDateTime.now());
        disabledUser.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(disabledUser);

        try {
            var request = new com.cas.auth.LoginRequest();
            request.setUsername("disableduser");
            request.setPassword("123456");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "/api/v1/auth/login", request, Map.class);

            Map body = response.getBody();
            assertEquals(409, body.get("code"));
            assertEquals("账号已被禁用", body.get("message"));
        } finally {
            userMapper.deleteById(disabledUser.getId());
        }
    }

    // ==================== /me Tests ====================

    @Test
    void should_return_user_info_when_token_valid() {
        String token = loginAndGetToken("student1", "123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/auth/me", HttpMethod.GET, entity, Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map body = response.getBody();
        assertEquals(200, body.get("code"));

        Map data = (Map) body.get("data");
        assertEquals("student1", data.get("username"));
        assertEquals("STUDENT", data.get("role"));
        assertEquals("张三", data.get("realName"));
    }

    @Test
    void should_return_401_when_no_token() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/v1/auth/me", Map.class);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void should_return_401_when_invalid_token() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid-token-here");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/auth/me", HttpMethod.GET, entity, Map.class);
        assertEquals(401, response.getStatusCode().value());
    }

    // ==================== Role Access Tests ====================

    @Test
    void should_deny_student_accessing_admin_endpoint() {
        String token = loginAndGetToken("student1", "123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/semesters", HttpMethod.GET, entity, Map.class);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void should_deny_teacher_accessing_student_endpoint() {
        String token = loginAndGetToken("teacher1", "123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/enrollments", HttpMethod.GET, entity, Map.class);

        assertEquals(403, response.getStatusCode().value());
    }
}