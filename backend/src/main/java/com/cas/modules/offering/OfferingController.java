package com.cas.modules.offering;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.common.result.ApiResponse;
import com.cas.common.result.PageResponse;
import com.cas.modules.offering.dto.OfferingRequest;
import com.cas.modules.offering.dto.OfferingVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;

    // Admin: CRUD
    @GetMapping("/api/v1/admin/offerings")
    public ApiResponse<PageResponse<OfferingVO>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(PageResponse.of(offeringService.listOfferings(page, pageSize)));
    }

    @PostMapping("/api/v1/admin/offerings")
    public ApiResponse<OfferingVO> create(@Valid @RequestBody OfferingRequest request) {
        return ApiResponse.success(offeringService.createOffering(request));
    }

    @PutMapping("/api/v1/admin/offerings/{id}")
    public ApiResponse<OfferingVO> update(@PathVariable Long id,
                                          @Valid @RequestBody OfferingRequest request) {
        return ApiResponse.success(offeringService.updateOffering(id, request));
    }

    // Student: course hall
    @GetMapping("/api/v1/courses")
    public ApiResponse<PageResponse<OfferingVO>> studentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        return ApiResponse.success(PageResponse.of(
                offeringService.listStudentOfferings(page, pageSize, keyword, type)));
    }

    @GetMapping("/api/v1/courses/{id}")
    public ApiResponse<OfferingVO> studentDetail(@PathVariable Long id) {
        return ApiResponse.success(offeringService.getStudentOfferingDetail(id));
    }
}