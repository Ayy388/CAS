package com.cas.modules.enrollment;

import com.cas.common.result.ApiResponse;
import com.cas.modules.enrollment.dto.EnrollRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/enrollments")
    public ApiResponse<?> enroll(@Valid @RequestBody EnrollRequest request,
                                 Authentication authentication) {
        Long studentId = (Long) authentication.getPrincipal();
        return ApiResponse.success(enrollmentService.enroll(request.getOfferingId(), studentId));
    }

    @GetMapping("/student/enrollments")
    public ApiResponse<?> myEnrollments(Authentication authentication) {
        Long studentId = (Long) authentication.getPrincipal();
        return ApiResponse.success(enrollmentService.listMyEnrollments(studentId));
    }

    @DeleteMapping("/enrollments/{id}")
    public ApiResponse<Void> drop(@PathVariable Long id,
                                  Authentication authentication) {
        Long studentId = (Long) authentication.getPrincipal();
        enrollmentService.dropEnrollment(id, studentId);
        return ApiResponse.success();
    }
}