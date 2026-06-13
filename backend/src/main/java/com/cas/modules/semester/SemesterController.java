package com.cas.modules.semester;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.common.result.ApiResponse;
import com.cas.common.result.PageResponse;
import com.cas.modules.semester.dto.SemesterRequest;
import com.cas.modules.semester.dto.SemesterVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping
    public ApiResponse<PageResponse<SemesterVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<SemesterVO> result = semesterService.listSemesters(page, pageSize);
        return ApiResponse.success(PageResponse.of(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<SemesterVO> getById(@PathVariable Long id) {
        SemesterVO vo = semesterService.getSemesterById(id);
        return ApiResponse.success(vo);
    }

    @PostMapping
    public ApiResponse<SemesterVO> create(@Valid @RequestBody SemesterRequest request) {
        SemesterVO vo = semesterService.createSemester(request);
        return ApiResponse.success(vo);
    }

    @PutMapping("/{id}")
    public ApiResponse<SemesterVO> update(@PathVariable Long id,
                                          @Valid @RequestBody SemesterRequest request) {
        SemesterVO vo = semesterService.updateSemester(id, request);
        return ApiResponse.success(vo);
    }

    @PatchMapping("/{id}/activate")
    public ApiResponse<SemesterVO> activate(@PathVariable Long id) {
        SemesterVO vo = semesterService.toggleActivate(id);
        return ApiResponse.success(vo);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        semesterService.deleteSemester(id);
        return ApiResponse.success();
    }
}