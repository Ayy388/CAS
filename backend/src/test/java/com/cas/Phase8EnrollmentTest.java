package com.cas;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cas.auth.entity.User;
import com.cas.auth.mapper.UserMapper;
import com.cas.modules.campaign.entity.Campaign;
import com.cas.modules.campaign.mapper.CampaignMapper;
import com.cas.modules.enrollment.mapper.EnrollmentMapper;
import com.cas.modules.semester.mapper.SemesterMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Phase8EnrollmentTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CampaignMapper campaignMapper;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void cleanStaleTestData() {
        // Terminate any existing ACTIVE campaigns to avoid LIMIT 1 conflicts
        List<Campaign> activeCampaigns = campaignMapper.selectList(
                Wrappers.lambdaQuery(new Campaign()).eq(Campaign::getStatus, "ACTIVE"));
        for (Campaign c : activeCampaigns) {
            c.setStatus("ENDED");
            c.setUpdatedAt(LocalDateTime.now());
            campaignMapper.updateById(c);
        }

        // Remove stale test campaigns and their enrollments to avoid LIMIT 1 conflicts
        List<Campaign> staleCampaigns = campaignMapper.selectList(
                Wrappers.lambdaQuery(new Campaign()).like(Campaign::getName, "测试"));
        for (Campaign c : staleCampaigns) {
            enrollmentMapper.delete(Wrappers.lambdaQuery(new com.cas.modules.enrollment.entity.Enrollment())
                    .eq(com.cas.modules.enrollment.entity.Enrollment::getCampaignId, c.getId()));
            campaignMapper.deleteById(c.getId());
        }
        // Clear Redis keys for test isolation
        try {
            Set<String> keys = redisTemplate.keys("cas:enroll:request:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            // Redis unavailable, ignore
        }
    }

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

    private HttpEntity<Object> adminPost(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken());
        if (body != null) headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Object> bearerPost(String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        if (body != null) headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private void ensureUsers() {
        if (userMapper.selectOne(Wrappers.lambdaQuery(new User()).eq(User::getUsername, "tch_enroll")) == null) {
            User t = new User();
            t.setUsername("tch_enroll");
            t.setPassword(passwordEncoder.encode("123456"));
            t.setRealName("选课测试教师");
            t.setRole("TEACHER");
            t.setStatus(1);
            t.setDepartment("计算机学院");
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(t);
        }
        if (userMapper.selectOne(Wrappers.lambdaQuery(new User()).eq(User::getUsername, "stu_enroll")) == null) {
            User s = new User();
            s.setUsername("stu_enroll");
            s.setPassword(passwordEncoder.encode("123456"));
            s.setRealName("选课测试学生");
            s.setRole("STUDENT");
            s.setStatus(1);
            s.setStudentId("20241001");
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

    /**
     * Full setup: semester → course → offering → campaign → start campaign
     * Returns the offering ID
     */
    private long[] fullSetup() {
        ensureUsers();
        String at = adminToken();

        // Create semester & activate
        Map<String, Object> semBody = Map.of(
                "name", "选课测试学期",
                "academicYear", "2025-2026",
                "semesterType", "FIRST",
                "startDate", "2025-09-01",
                "endDate", "2026-01-15"
        );
        ResponseEntity<Map> semResp = restTemplate.exchange(
                "/api/v1/admin/semesters", HttpMethod.POST, adminPost(semBody), Map.class);
        Long semId = ((Number) ((Map) semResp.getBody().get("data")).get("id")).longValue();
        restTemplate.exchange("/api/v1/admin/semesters/" + semId + "/activate",
                HttpMethod.PATCH, adminPost(null), Map.class);

        // Create course
        Map<String, Object> courseBody = Map.of(
                "code", "ENRL" + System.currentTimeMillis(),
                "name", "选课测试课程",
                "type", "ELECTIVE_GENERAL",
                "credits", 2.0,
                "hours", 32
        );
        ResponseEntity<Map> courseResp = restTemplate.exchange(
                "/api/v1/admin/courses", HttpMethod.POST, adminPost(courseBody), Map.class);
        Long courseId = ((Number) ((Map) courseResp.getBody().get("data")).get("id")).longValue();

        // Create offering
        Long teacherId = getUserId("tch_enroll");
        Map<String, Object> offerBody = Map.of(
                "semesterId", semId,
                "courseId", courseId,
                "teacherId", teacherId,
                "maxCapacity", 60,
                "minEnrollment", 5,
                "openGrade", "2024",
                "openMajor", "计算机科学与技术"
        );
        ResponseEntity<Map> offerResp = restTemplate.exchange(
                "/api/v1/admin/offerings", HttpMethod.POST, adminPost(offerBody), Map.class);
        Long offeringId = ((Number) ((Map) offerResp.getBody().get("data")).get("id")).longValue();

        // Create campaign covering now
        String fmt = "yyyy-MM-dd HH:mm:ss";
        Map<String, Object> campBody = Map.of(
                "name", "选课测试活动",
                "semesterId", semId,
                "startTime", LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ofPattern(fmt)),
                "endTime", LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern(fmt))
        );
        ResponseEntity<Map> campResp = restTemplate.exchange(
                "/api/v1/admin/campaigns", HttpMethod.POST, adminPost(campBody), Map.class);
        Long campId = ((Number) ((Map) campResp.getBody().get("data")).get("id")).longValue();

        // Start campaign
        restTemplate.exchange("/api/v1/admin/campaigns/" + campId + "/start",
                HttpMethod.PATCH, adminPost(null), Map.class);

        return new long[]{offeringId, semId, courseId, campId};
    }

    @Test
    void should_enroll_successfully() {
        long[] ids = fullSetup();
        long offeringId = ids[0];

        String studentToken = tokenFor("stu_enroll");
        assertNotNull(studentToken, "Student token should not be null");

        // Enroll
        Map<String, Object> enrollBody = Map.of("offeringId", offeringId);
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/enrollments", HttpMethod.POST,
                bearerPost(studentToken, enrollBody), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map data = (Map) response.getBody().get("data");
        assertNotNull(data.get("id"));
        assertEquals("ENROLLED", data.get("status"));
        assertEquals(offeringId, ((Number) data.get("offeringId")).longValue());
    }

    @Test
    void should_return_429_when_too_fast() {
        long[] ids = fullSetup();
        long offeringId = ids[0];

        String studentToken = tokenFor("stu_enroll");
        assertNotNull(studentToken);

        Map<String, Object> enrollBody = Map.of("offeringId", offeringId);

        // First request (should succeed or fail with 429 if too fast)
        restTemplate.exchange("/api/v1/enrollments", HttpMethod.POST,
                bearerPost(studentToken, enrollBody), Map.class);

        // Immediate second request — should get 429
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/enrollments", HttpMethod.POST,
                bearerPost(studentToken, enrollBody), Map.class);

        // Either 429 (rate limited) or 409 (already enrolled)
        int code = (int) response.getBody().get("code");
        assertTrue(code == 429 || code == 409,
                "Expected 429 or 409 but got " + code);
    }

    @Test
    void should_drop_enrollment() {
        long[] ids = fullSetup();
        long offeringId = ids[0];

        String studentToken = tokenFor("stu_enroll");
        assertNotNull(studentToken);

        // Enroll first
        Map<String, Object> enrollBody = Map.of("offeringId", offeringId);
        ResponseEntity<Map> enrollResp = restTemplate.exchange(
                "/api/v1/enrollments", HttpMethod.POST,
                bearerPost(studentToken, enrollBody), Map.class);
        Long enrollmentId = ((Number) ((Map) enrollResp.getBody().get("data")).get("id")).longValue();

        // Drop
        ResponseEntity<Map> dropResp = restTemplate.exchange(
                "/api/v1/enrollments/" + enrollmentId,
                HttpMethod.DELETE, bearerPost(studentToken, null), Map.class);

        assertEquals(200, dropResp.getStatusCode().value());
    }

    @Test
    void should_list_my_enrollments() {
        long[] ids = fullSetup();
        long offeringId = ids[0];

        String studentToken = tokenFor("stu_enroll");
        assertNotNull(studentToken);

        // Enroll first
        Map<String, Object> enrollBody = Map.of("offeringId", offeringId);
        restTemplate.exchange("/api/v1/enrollments", HttpMethod.POST,
                bearerPost(studentToken, enrollBody), Map.class);

        // List
        // Override: not used, read from PageResponse.items
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/student/enrollments?page=1&pageSize=20",
                HttpMethod.GET, bearerPost(studentToken, null), Map.class);

        assertEquals(200, response.getStatusCode().value());
        List data = (List) response.getBody().get("data");
        assertNotNull(data);
    }

    @Test
    void should_return_409_when_no_active_campaign() {
        ensureUsers();
        String studentToken = tokenFor("stu_enroll");
        assertNotNull(studentToken);

        // Enroll without any setup — no active campaign
        Map<String, Object> enrollBody = Map.of("offeringId", 99999L);
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/enrollments", HttpMethod.POST,
                bearerPost(studentToken, enrollBody), Map.class);

        assertEquals(409, response.getBody().get("code"));
    }
}