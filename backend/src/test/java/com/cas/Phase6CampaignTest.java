package com.cas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Phase6CampaignTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String adminToken() {
        var req = new com.cas.auth.LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/auth/login", req, Map.class);
        Map data = (Map) resp.getBody().get("data");
        return (String) data.get("token");
    }

    private HttpEntity<Object> authPost(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private Long createTestSemester() {
        Map<String, Object> body = Map.of(
                "name", "测试学期-活动",
                "academicYear", "2025-2026",
                "semesterType", "FIRST",
                "startDate", "2025-09-01",
                "endDate", "2026-01-15"
        );
        ResponseEntity<Map> resp = restTemplate.exchange(
                "/api/v1/admin/semesters", HttpMethod.POST, authPost(body), Map.class);
        return ((Number) ((Map) resp.getBody().get("data")).get("id")).longValue();
    }

    @Test
    void should_create_campaign_with_PENDING_status() {
        Long semesterId = createTestSemester();

        Map<String, Object> body = Map.of(
                "name", "测试选课活动-创建",
                "semesterId", semesterId,
                "startTime", "2025-09-01 09:00:00",
                "endTime", "2025-09-05 17:00:00"
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/campaigns", HttpMethod.POST, authPost(body), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map data = (Map) response.getBody().get("data");
        assertNotNull(data.get("id"));
        assertEquals("PENDING", data.get("status"));
        assertEquals("测试选课活动-创建", data.get("name"));
    }

    @Test
    void should_return_409_when_duplicate_active_campaign_in_semester() {
        Long semesterId = createTestSemester();

        Map<String, Object> body = Map.of(
                "name", "重复活动测试",
                "semesterId", semesterId,
                "startTime", "2025-10-01 09:00:00",
                "endTime", "2025-10-05 17:00:00"
        );

        // First create
        restTemplate.exchange("/api/v1/admin/campaigns", HttpMethod.POST, authPost(body), Map.class);

        // Second create — should fail with 409
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/campaigns", HttpMethod.POST, authPost(body), Map.class);

        assertEquals(409, response.getBody().get("code"));
        assertEquals("该学期已有进行中或待开始的选课活动", response.getBody().get("message"));
    }

    @Test
    void should_start_campaign() {
        Long semesterId = createTestSemester();

        Map<String, Object> body = Map.of(
                "name", "测试选课活动-启动",
                "semesterId", semesterId,
                "startTime", "2025-09-01 09:00:00",
                "endTime", "2025-09-05 17:00:00"
        );

        ResponseEntity<Map> createResp = restTemplate.exchange(
                "/api/v1/admin/campaigns", HttpMethod.POST, authPost(body), Map.class);
        Long id = ((Number) ((Map) createResp.getBody().get("data")).get("id")).longValue();

        // Start
        ResponseEntity<Map> startResp = restTemplate.exchange(
                "/api/v1/admin/campaigns/" + id + "/start",
                HttpMethod.PATCH, authPost(null), Map.class);

        assertEquals(200, startResp.getStatusCode().value());
        assertEquals("ACTIVE", ((Map) startResp.getBody().get("data")).get("status"));
    }

    @Test
    void should_end_campaign() {
        Long semesterId = createTestSemester();

        Map<String, Object> body = Map.of(
                "name", "测试选课活动-结束",
                "semesterId", semesterId,
                "startTime", "2025-09-01 09:00:00",
                "endTime", "2025-09-05 17:00:00"
        );

        ResponseEntity<Map> createResp = restTemplate.exchange(
                "/api/v1/admin/campaigns", HttpMethod.POST, authPost(body), Map.class);
        Long id = ((Number) ((Map) createResp.getBody().get("data")).get("id")).longValue();

        // Start
        restTemplate.exchange("/api/v1/admin/campaigns/" + id + "/start",
                HttpMethod.PATCH, authPost(null), Map.class);

        // End
        ResponseEntity<Map> endResp = restTemplate.exchange(
                "/api/v1/admin/campaigns/" + id + "/end",
                HttpMethod.PATCH, authPost(null), Map.class);

        assertEquals(200, endResp.getStatusCode().value());
        assertEquals("ENDED", ((Map) endResp.getBody().get("data")).get("status"));
    }

    @Test
    void should_return_409_when_start_non_pending_campaign() {
        Long semesterId = createTestSemester();

        Map<String, Object> body = Map.of(
                "name", "测试-已结束活动",
                "semesterId", semesterId,
                "startTime", "2025-08-01 09:00:00",
                "endTime", "2025-08-05 17:00:00"
        );

        ResponseEntity<Map> createResp = restTemplate.exchange(
                "/api/v1/admin/campaigns", HttpMethod.POST, authPost(body), Map.class);
        Long id = ((Number) ((Map) createResp.getBody().get("data")).get("id")).longValue();

        // Start then end
        restTemplate.exchange("/api/v1/admin/campaigns/" + id + "/start", HttpMethod.PATCH, authPost(null), Map.class);
        restTemplate.exchange("/api/v1/admin/campaigns/" + id + "/end", HttpMethod.PATCH, authPost(null), Map.class);

        // Try to start again — should fail
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/campaigns/" + id + "/start",
                HttpMethod.PATCH, authPost(null), Map.class);

        assertEquals(409, response.getBody().get("code"));
    }

    @Test
    void should_list_campaigns_with_pagination() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/campaigns?page=1&pageSize=20",
                HttpMethod.GET, authPost(null), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map data = (Map) response.getBody().get("data");
        assertNotNull(data.get("items"));
        assertNotNull(data.get("total"));
    }
}