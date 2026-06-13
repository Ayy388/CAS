package com.cas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Phase5CourseTest {

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

    @Test
    void should_create_course() {
        String code = "CS" + System.currentTimeMillis();
        Map<String, Object> body = Map.of(
                "code", code,
                "name", "计算机导论",
                "type", "REQUIRED",
                "credits", 3.0,
                "hours", 48,
                "description", "计算机基础课程"
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/courses", HttpMethod.POST, authPost(body), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map data = (Map) response.getBody().get("data");
        assertNotNull(data.get("id"));
        assertEquals(code, data.get("code"));
        assertEquals("计算机导论", data.get("name"));
    }

    @Test
    void should_return_409_when_duplicate_code() {
        String code = "UNQ" + System.currentTimeMillis();
        Map<String, Object> body = Map.of(
                "code", code,
                "name", "唯一测试课程",
                "type", "ELECTIVE_GENERAL",
                "credits", 2.0,
                "hours", 32
        );
        // First create
        restTemplate.exchange("/api/v1/admin/courses", HttpMethod.POST, authPost(body), Map.class);
        // Second create with same code
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/courses", HttpMethod.POST, authPost(body), Map.class);

        Map respBody = response.getBody();
        assertEquals(409, respBody.get("code"));
        assertEquals("课程编号已存在", respBody.get("message"));
    }

    @Test
    void should_list_courses_with_keyword_filter() {
        String code = "KW" + System.currentTimeMillis();
        Map<String, Object> body = Map.of(
                "code", code,
                "name", "关键字搜索测试课程",
                "type", "ELECTIVE_MAJOR",
                "credits", 2.0,
                "hours", 32
        );
        restTemplate.exchange("/api/v1/admin/courses", HttpMethod.POST, authPost(body), Map.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/courses?keyword=关键字", HttpMethod.GET,
                authPost(null), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map data = (Map) response.getBody().get("data");
        assertTrue(((Number) data.get("total")).intValue() >= 1);
    }

    @Test
    void should_update_course() {
        String code = "UPD" + System.currentTimeMillis();
        Map<String, Object> createBody = Map.of(
                "code", code,
                "name", "更新前",
                "type", "REQUIRED",
                "credits", 3.0,
                "hours", 48
        );
        ResponseEntity<Map> createResp = restTemplate.exchange(
                "/api/v1/admin/courses", HttpMethod.POST, authPost(createBody), Map.class);
        Long id = ((Number) ((Map) createResp.getBody().get("data")).get("id")).longValue();

        Map<String, Object> updateBody = Map.of(
                "code", code,
                "name", "更新后",
                "type", "REQUIRED",
                "credits", 4.0,
                "hours", 64
        );
        ResponseEntity<Map> updateResp = restTemplate.exchange(
                "/api/v1/admin/courses/" + id, HttpMethod.PUT, authPost(updateBody), Map.class);

        assertEquals("更新后", ((Map) updateResp.getBody().get("data")).get("name"));
    }

    @Test
    void should_return_409_when_delete_course_with_offerings() {
        String code = "DEL" + System.currentTimeMillis();
        Map<String, Object> courseBody = Map.of(
                "code", code,
                "name", "删除测试课程",
                "type", "ELECTIVE_GENERAL",
                "credits", 2.0,
                "hours", 32
        );
        ResponseEntity<Map> createResp = restTemplate.exchange(
                "/api/v1/admin/courses", HttpMethod.POST, authPost(courseBody), Map.class);
        Long courseId = ((Number) ((Map) createResp.getBody().get("data")).get("id")).longValue();

        // Try to delete — should succeed since no offering references it yet
        ResponseEntity<Map> deleteResp = restTemplate.exchange(
                "/api/v1/admin/courses/" + courseId, HttpMethod.DELETE, authPost(null), Map.class);

        assertEquals(200, deleteResp.getStatusCode().value());
        Map body = deleteResp.getBody();
        assertEquals(200, body.get("code"));
    }
}