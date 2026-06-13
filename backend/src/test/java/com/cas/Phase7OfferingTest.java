package com.cas;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cas.auth.entity.User;
import com.cas.auth.mapper.UserMapper;
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
class Phase7OfferingTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken() {
        var req = new com.cas.auth.LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/auth/login", req, Map.class);
        Map data = (Map) resp.getBody().get("data");
        return (String) data.get("token");
    }

    private String tokenFor(String username, String password) {
        var req = new com.cas.auth.LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/auth/login", req, Map.class);
        Map data = (Map) resp.getBody().get("data");
        return (String) data.get("token");
    }

    private HttpEntity<Object> authPost(String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Object> adminPost(Object body) {
        return authPost(adminToken(), body);
    }

    private void ensureTestUsers() {
        // Create teacher if not exists
        if (userMapper.selectOne(Wrappers.lambdaQuery(new User()).eq(User::getUsername, "teacher01")) == null) {
            User t = new User();
            t.setUsername("teacher01");
            t.setPassword(passwordEncoder.encode("123456"));
            t.setRealName("测试教师");
            t.setRole("TEACHER");
            t.setStatus(1);
            t.setDepartment("计算机学院");
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(t);
        }
        // Create student if not exists
        if (userMapper.selectOne(Wrappers.lambdaQuery(new User()).eq(User::getUsername, "student01")) == null) {
            User s = new User();
            s.setUsername("student01");
            s.setPassword(passwordEncoder.encode("123456"));
            s.setRealName("测试学生");
            s.setRole("STUDENT");
            s.setStatus(1);
            s.setStudentId("2024001");
            s.setDepartment("计算机学院");
            s.setMajor("计算机科学与技术");
            s.setGrade("2024");
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(s);
        }
    }

    private Long createTestSemester() {
        Map<String, Object> body = Map.of(
                "name", "测试学期-开课",
                "academicYear", "2025-2026",
                "semesterType", "FIRST",
                "startDate", "2025-09-01",
                "endDate", "2026-01-15"
        );
        ResponseEntity<Map> resp = restTemplate.exchange(
                "/api/v1/admin/semesters", HttpMethod.POST, adminPost(body), Map.class);
        return ((Number) ((Map) resp.getBody().get("data")).get("id")).longValue();
    }

    private Long createTestCourse() {
        Map<String, Object> body = Map.of(
                "code", "OFFER" + System.currentTimeMillis(),
                "name", "开课测试课程",
                "type", "REQUIRED",
                "credits", 3.0,
                "hours", 48
        );
        ResponseEntity<Map> resp = restTemplate.exchange(
                "/api/v1/admin/courses", HttpMethod.POST, adminPost(body), Map.class);
        return ((Number) ((Map) resp.getBody().get("data")).get("id")).longValue();
    }

    private Long getTeacherId() {
        ensureTestUsers();
        return userMapper.selectOne(Wrappers.lambdaQuery(new User()).eq(User::getUsername, "teacher01")).getId();
    }

    private Long getStudentId() {
        ensureTestUsers();
        return userMapper.selectOne(Wrappers.lambdaQuery(new User()).eq(User::getUsername, "student01")).getId();
    }

    @Test
    void should_create_offering() {
        Long semesterId = createTestSemester();
        Long courseId = createTestCourse();
        Long teacherId = getTeacherId();

        Map<String, Object> body = Map.of(
                "semesterId", semesterId,
                "courseId", courseId,
                "teacherId", teacherId,
                "maxCapacity", 60,
                "minEnrollment", 10,
                "location", "教学楼301",
                "schedule", "周一1-2节",
                "openGrade", "2024",
                "openMajor", "计算机科学与技术"
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/offerings", HttpMethod.POST, adminPost(body), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map data = (Map) response.getBody().get("data");
        assertNotNull(data.get("id"));
        assertEquals(60, ((Number) data.get("maxCapacity")).intValue());
        assertEquals("PENDING", data.get("status"));
        assertEquals(60, ((Number) data.get("seatsRemaining")).intValue());
    }

    @Test
    void should_list_student_courses() {
        String studentToken = tokenFor("student01", "123456");
        if (studentToken == null) {
            ensureTestUsers();
            studentToken = tokenFor("student01", "123456");
        }

        // Create a semester + offering first
        Long semesterId = createTestSemester();
        // Activate semester
        ResponseEntity<Map> allSem = restTemplate.exchange(
                "/api/v1/admin/semesters?page=1&pageSize=100", HttpMethod.GET, adminPost(null), Map.class);
        // Activate our semester
        restTemplate.exchange("/api/v1/admin/semesters/" + semesterId + "/activate",
                HttpMethod.PATCH, adminPost(null), Map.class);

        Long courseId = createTestCourse();
        Long teacherId = getTeacherId();

        Map<String, Object> offerBody = Map.of(
                "semesterId", semesterId,
                "courseId", courseId,
                "teacherId", teacherId,
                "maxCapacity", 60,
                "minEnrollment", 10
        );
        restTemplate.exchange("/api/v1/admin/offerings", HttpMethod.POST, adminPost(offerBody), Map.class);

        // Student list
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/courses?page=1&pageSize=20",
                HttpMethod.GET, authPost(studentToken, null), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map data = (Map) response.getBody().get("data");
        assertNotNull(data.get("items"));
    }

    @Test
    void should_get_student_course_detail() {
        String studentToken = tokenFor("student01", "123456");
        if (studentToken == null) {
            ensureTestUsers();
            studentToken = tokenFor("student01", "123456");
        }

        Long semesterId = createTestSemester();
        restTemplate.exchange("/api/v1/admin/semesters/" + semesterId + "/activate",
                HttpMethod.PATCH, adminPost(null), Map.class);

        Long courseId = createTestCourse();
        Long teacherId = getTeacherId();

        Map<String, Object> offerBody = Map.of(
                "semesterId", semesterId,
                "courseId", courseId,
                "teacherId", teacherId,
                "maxCapacity", 60,
                "minEnrollment", 10
        );
        ResponseEntity<Map> createResp = restTemplate.exchange(
                "/api/v1/admin/offerings", HttpMethod.POST, adminPost(offerBody), Map.class);
        Long offeringId = ((Number) ((Map) createResp.getBody().get("data")).get("id")).longValue();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/courses/" + offeringId,
                HttpMethod.GET, authPost(studentToken, null), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map data = (Map) response.getBody().get("data");
        assertEquals(offeringId, ((Number) data.get("id")).longValue());
        assertNotNull(data.get("courseName"));
    }

    @Test
    void should_return_404_when_offering_not_found() {
        String studentToken = tokenFor("student01", "123456");
        if (studentToken == null) {
            ensureTestUsers();
            studentToken = tokenFor("student01", "123456");
        }

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/courses/99999",
                HttpMethod.GET, authPost(studentToken, null), Map.class);

        assertEquals(404, response.getBody().get("code"));
    }
}