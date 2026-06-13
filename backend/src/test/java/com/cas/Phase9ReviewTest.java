package com.cas;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cas.auth.entity.User;
import com.cas.auth.mapper.UserMapper;
import com.cas.modules.campaign.entity.Campaign;
import com.cas.modules.campaign.mapper.CampaignMapper;
import com.cas.modules.enrollment.mapper.EnrollmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Phase9ReviewTest {

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

    @BeforeEach
    void cleanStaleTestData() {
        List<Campaign> staleCampaigns = campaignMapper.selectList(
                Wrappers.lambdaQuery(new Campaign()).like(Campaign::getName, "测试"));
        for (Campaign c : staleCampaigns) {
            enrollmentMapper.delete(Wrappers.lambdaQuery(new com.cas.modules.enrollment.entity.Enrollment())
                    .eq(com.cas.modules.enrollment.entity.Enrollment::getCampaignId, c.getId()));
            campaignMapper.deleteById(c.getId());
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
        if (userMapper.selectOne(Wrappers.lambdaQuery(new User()).eq(User::getUsername, "tch_review")) == null) {
            User t = new User();
            t.setUsername("tch_review");
            t.setPassword(passwordEncoder.encode("123456"));
            t.setRealName("审核测试教师");
            t.setRole("TEACHER");
            t.setStatus(1);
            t.setDepartment("计算机学院");
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(t);
        }
        if (userMapper.selectOne(Wrappers.lambdaQuery(new User()).eq(User::getUsername, "stu_review")) == null) {
            User s = new User();
            s.setUsername("stu_review");
            s.setPassword(passwordEncoder.encode("123456"));
            s.setRealName("审核测试学生");
            s.setRole("STUDENT");
            s.setStatus(1);
            s.setStudentId("20242001");
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
     * Creates a full setup + enrollment, returns [offeringId, campaignId]
     */
    private long[] setupWithEnrollment() {
        ensureUsers();
        String at = adminToken();

        // Semester
        Map<String, Object> semBody = Map.of(
                "name", "审核测试学期",
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

        // Course
        Map<String, Object> courseBody = Map.of(
                "code", "REVW" + System.currentTimeMillis(),
                "name", "审核测试课程",
                "type", "ELECTIVE_GENERAL",
                "credits", 2.0,
                "hours", 32
        );
        ResponseEntity<Map> courseResp = restTemplate.exchange(
                "/api/v1/admin/courses", HttpMethod.POST, adminPost(courseBody), Map.class);
        Long courseId = ((Number) ((Map) courseResp.getBody().get("data")).get("id")).longValue();

        // Offering
        Long teacherId = getUserId("tch_review");
        Map<String, Object> offerBody = Map.of(
                "semesterId", semId,
                "courseId", courseId,
                "teacherId", teacherId,
                "maxCapacity", 60,
                "minEnrollment", 5
        );
        ResponseEntity<Map> offerResp = restTemplate.exchange(
                "/api/v1/admin/offerings", HttpMethod.POST, adminPost(offerBody), Map.class);
        Long offeringId = ((Number) ((Map) offerResp.getBody().get("data")).get("id")).longValue();

        // Campaign
        String fmt = "yyyy-MM-dd HH:mm:ss";
        Map<String, Object> campBody = Map.of(
                "name", "审核测试活动",
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

        // Enroll student
        String studentToken = tokenFor("stu_review");
        Map<String, Object> enrollBody = Map.of("offeringId", offeringId);
        restTemplate.exchange("/api/v1/enrollments", HttpMethod.POST,
                bearerPost(studentToken, enrollBody), Map.class);

        return new long[]{offeringId, campId};
    }

    @Test
    void should_list_reviews() {
        setupWithEnrollment();

        // List reviews
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/review?page=1&pageSize=20",
                HttpMethod.GET, adminPost(null), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map data = (Map) response.getBody().get("data");
        assertNotNull(data.get("items"));
    }

    @Test
    void should_approve_offering() {
        long[] ids = setupWithEnrollment();
        long offeringId = ids[0];

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/review/" + offeringId + "/approve",
                HttpMethod.POST, adminPost(null), Map.class);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void should_reject_offering() {
        long[] ids = setupWithEnrollment();
        long offeringId = ids[0];

        // Use a fresh offering for reject (can't approve then reject)
        // Create another offering for reject test
        ensureUsers();
        String at = adminToken();

        Map<String, Object> semBody = Map.of(
                "name", "审核测试学期2",
                "academicYear", "2025-2026",
                "semesterType", "SECOND",
                "startDate", "2026-02-15",
                "endDate", "2026-07-15"
        );
        ResponseEntity<Map> semResp = restTemplate.exchange(
                "/api/v1/admin/semesters", HttpMethod.POST, adminPost(semBody), Map.class);
        Long semId = ((Number) ((Map) semResp.getBody().get("data")).get("id")).longValue();
        restTemplate.exchange("/api/v1/admin/semesters/" + semId + "/activate",
                HttpMethod.PATCH, adminPost(null), Map.class);

        Map<String, Object> courseBody = Map.of(
                "code", "REVW2" + System.currentTimeMillis(),
                "name", "审核测试课程2",
                "type", "ELECTIVE_GENERAL",
                "credits", 2.0,
                "hours", 32
        );
        ResponseEntity<Map> courseResp = restTemplate.exchange(
                "/api/v1/admin/courses", HttpMethod.POST, adminPost(courseBody), Map.class);
        Long courseId = ((Number) ((Map) courseResp.getBody().get("data")).get("id")).longValue();

        Long teacherId = getUserId("tch_review");
        Map<String, Object> offerBody = Map.of(
                "semesterId", semId,
                "courseId", courseId,
                "teacherId", teacherId,
                "maxCapacity", 60,
                "minEnrollment", 5
        );
        ResponseEntity<Map> offerResp = restTemplate.exchange(
                "/api/v1/admin/offerings", HttpMethod.POST, adminPost(offerBody), Map.class);
        Long offeringId2 = ((Number) ((Map) offerResp.getBody().get("data")).get("id")).longValue();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/review/" + offeringId2 + "/reject",
                HttpMethod.POST, adminPost(null), Map.class);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void should_return_409_when_already_reviewed() {
        long[] ids = setupWithEnrollment();
        long offeringId = ids[0];

        // Approve first
        restTemplate.exchange("/api/v1/admin/review/" + offeringId + "/approve",
                HttpMethod.POST, adminPost(null), Map.class);

        // Try to approve again — should fail with 409
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/review/" + offeringId + "/approve",
                HttpMethod.POST, adminPost(null), Map.class);

        assertEquals(409, response.getBody().get("code"));
    }
}