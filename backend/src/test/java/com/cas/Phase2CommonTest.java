package com.cas;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.common.enums.*;
import com.cas.common.exception.BusinessException;
import com.cas.common.result.ApiResponse;
import com.cas.common.result.PageResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Phase2CommonTest {

    // ==================== ApiResponse ====================

    @Test
    void should_return_200_and_data_when_success() {
        ApiResponse<String> resp = ApiResponse.success("hello");
        assertEquals(200, resp.getCode());
        assertEquals("success", resp.getMessage());
        assertEquals("hello", resp.getData());
    }

    @Test
    void should_return_200_with_null_data_when_success_no_arg() {
        ApiResponse<Void> resp = ApiResponse.success();
        assertEquals(200, resp.getCode());
        assertNull(resp.getData());
    }

    @Test
    void should_return_error_with_code_and_message() {
        ApiResponse<Void> resp = ApiResponse.error(409, "业务冲突");
        assertEquals(409, resp.getCode());
        assertEquals("业务冲突", resp.getMessage());
        assertNull(resp.getData());
    }

    // ==================== PageResponse ====================

    @Test
    void should_wrap_mybatis_plus_page_correctly() {
        IPage<String> mpPage = new Page<>(2, 20);
        mpPage.setRecords(List.of("a", "b"));
        mpPage.setTotal(100);

        PageResponse<String> resp = PageResponse.of(mpPage);

        assertEquals(2, resp.getItems().size());
        assertEquals(100, resp.getTotal());
        assertEquals(2, resp.getPage());
        assertEquals(20, resp.getPageSize());
    }

    // ==================== BusinessException ====================

    @Test
    void should_hold_code_and_message() {
        BusinessException e = new BusinessException(409, "自定义错误");
        assertEquals(409, e.getCode());
        assertEquals("自定义错误", e.getMessage());
    }

    @Test
    void should_default_to_code_409() {
        BusinessException e = new BusinessException("仅消息");
        assertEquals(409, e.getCode());
        assertEquals("仅消息", e.getMessage());
    }

    // ==================== Enum Consistency ====================

    @Test
    void user_role_enum_must_match_api_doc() {
        assertEquals("STUDENT", UserRole.STUDENT.name());
        assertEquals("TEACHER", UserRole.TEACHER.name());
        assertEquals("ADMIN", UserRole.ADMIN.name());
    }

    @Test
    void course_type_enum_must_match_api_doc() {
        assertEquals("REQUIRED", CourseType.REQUIRED.name());
        assertEquals("ELECTIVE_MAJOR", CourseType.ELECTIVE_MAJOR.name());
        assertEquals("ELECTIVE_GENERAL", CourseType.ELECTIVE_GENERAL.name());
    }

    @Test
    void campaign_status_enum_must_match_api_doc() {
        assertEquals("PENDING", CampaignStatus.PENDING.name());
        assertEquals("ACTIVE", CampaignStatus.ACTIVE.name());
        assertEquals("ENDED", CampaignStatus.ENDED.name());
    }

    @Test
    void offering_status_enum_must_match_api_doc() {
        assertEquals("PENDING", OfferingStatus.PENDING.name());
        assertEquals("APPROVED", OfferingStatus.APPROVED.name());
        assertEquals("REJECTED", OfferingStatus.REJECTED.name());
    }

    @Test
    void enrollment_status_enum_must_match_api_doc() {
        assertEquals("ENROLLED", EnrollmentStatus.ENROLLED.name());
        assertEquals("APPROVED", EnrollmentStatus.APPROVED.name());
        assertEquals("REJECTED", EnrollmentStatus.REJECTED.name());
        assertEquals("DROPPED", EnrollmentStatus.DROPPED.name());
    }

    @Test
    void notification_type_enum_must_match_api_doc() {
        assertEquals("APPROVED", NotificationType.APPROVED.name());
        assertEquals("REJECTED", NotificationType.REJECTED.name());
        assertEquals("SYSTEM", NotificationType.SYSTEM.name());
    }
}