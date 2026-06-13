package com.cas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Phase4SemesterTest {

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

    private HttpEntity<Object> authGet() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken());
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Object> authPost(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken());
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void should_create_semester_with_INACTIVE_status() {
        Map<String, Object> body = Map.of(
                "name", "2025-2026 第一学期",
                "academicYear", "2025-2026",
                "semesterType", "FIRST",
                "startDate", "2025-09-01",
                "endDate", "2026-01-15"
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/semesters", HttpMethod.POST, authPost(body), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map respBody = response.getBody();
        assertEquals(200, respBody.get("code"));

        Map data = (Map) respBody.get("data");
        assertNotNull(data.get("id"));
        assertEquals("2025-2026 第一学期", data.get("name"));
        assertEquals("INACTIVE", data.get("status"));
        assertEquals("FIRST", data.get("semesterType"));
    }

    @Test
    void should_list_semesters_with_pagination() {
        // First create a semester
        Map<String, Object> body = Map.of(
                "name", "2025-2026 第二学期",
                "academicYear", "2025-2026",
                "semesterType", "SECOND",
                "startDate", "2026-02-15",
                "endDate", "2026-07-15"
        );
        restTemplate.exchange("/api/v1/admin/semesters", HttpMethod.POST, authPost(body), Map.class);

        // Query list
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/semesters?page=1&pageSize=20", HttpMethod.GET, authGet(), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map respBody = response.getBody();
        Map data = (Map) respBody.get("data");
        assertTrue(((Number) data.get("total")).longValue() >= 1);
        assertNotNull(data.get("items"));
    }

    @Test
    void should_activate_semester_and_deactivate_others() {
        // Create semester A
        Map<String, Object> semesterA = Map.of(
                "name", "学期A",
                "academicYear", "2025-2026",
                "semesterType", "FIRST",
                "startDate", "2025-09-01",
                "endDate", "2026-01-15"
        );
        ResponseEntity<Map> createRespA = restTemplate.exchange(
                "/api/v1/admin/semesters", HttpMethod.POST, authPost(semesterA), Map.class);
        Long idA = ((Number) ((Map) createRespA.getBody().get("data")).get("id")).longValue();

        // Create semester B
        Map<String, Object> semesterB = Map.of(
                "name", "学期B",
                "academicYear", "2025-2026",
                "semesterType", "SECOND",
                "startDate", "2026-02-15",
                "endDate", "2026-07-15"
        );
        ResponseEntity<Map> createRespB = restTemplate.exchange(
                "/api/v1/admin/semesters", HttpMethod.POST, authPost(semesterB), Map.class);
        Long idB = ((Number) ((Map) createRespB.getBody().get("data")).get("id")).longValue();

        // Activate semester A
        ResponseEntity<Map> activateResp = restTemplate.exchange(
                "/api/v1/admin/semesters/" + idA + "/activate",
                HttpMethod.PATCH, authGet(), Map.class);

        assertEquals(200, activateResp.getStatusCode().value());
        Map activatedData = (Map) activateResp.getBody().get("data");
        assertEquals("ACTIVE", activatedData.get("status"));

        // Verify semester B is now INACTIVE
        ResponseEntity<Map> getRespB = restTemplate.exchange(
                "/api/v1/admin/semesters/" + idB, HttpMethod.GET, authGet(), Map.class);
        Map semesterBData = (Map) getRespB.getBody().get("data");
        assertEquals("INACTIVE", semesterBData.get("status"));
    }

    @Test
    void should_update_semester() {
        // Create
        Map<String, Object> createBody = Map.of(
                "name", "更新测试学期",
                "academicYear", "2024-2025",
                "semesterType", "FIRST",
                "startDate", "2024-09-01",
                "endDate", "2025-01-15"
        );
        ResponseEntity<Map> createResp = restTemplate.exchange(
                "/api/v1/admin/semesters", HttpMethod.POST, authPost(createBody), Map.class);
        Long id = ((Number) ((Map) createResp.getBody().get("data")).get("id")).longValue();

        // Update
        Map<String, Object> updateBody = Map.of(
                "name", "更新后学期名",
                "academicYear", "2024-2025",
                "semesterType", "FIRST",
                "startDate", "2024-09-01",
                "endDate", "2025-01-15"
        );
        ResponseEntity<Map> updateResp = restTemplate.exchange(
                "/api/v1/admin/semesters/" + id, HttpMethod.PUT,
                authPost(updateBody), Map.class);

        assertEquals(200, updateResp.getStatusCode().value());
        Map updatedData = (Map) updateResp.getBody().get("data");
        assertEquals("更新后学期名", updatedData.get("name"));
    }

    @Test
    void should_return_400_when_validation_fails() {
        Map<String, Object> invalidBody = Map.of(
                "name", "",
                "academicYear", "2025-2026"
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/semesters", HttpMethod.POST, authPost(invalidBody), Map.class);

        Map body = response.getBody();
        assertEquals(400, body.get("code"));
    }
}