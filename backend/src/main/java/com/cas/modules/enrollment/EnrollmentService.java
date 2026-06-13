package com.cas.modules.enrollment;

import com.cas.modules.enrollment.dto.EnrollmentVO;
import jakarta.validation.Valid;

import java.util.List;

public interface EnrollmentService {
    EnrollmentVO enroll(Long offeringId, Long studentId);
    void dropEnrollment(Long enrollmentId, Long studentId);
    List<EnrollmentVO> listMyEnrollments(Long studentId);
}